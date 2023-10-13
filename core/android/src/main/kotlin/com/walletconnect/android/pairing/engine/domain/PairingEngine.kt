package com.walletconnect.android.pairing.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.MALFORMED_PAIRING_URI_MESSAGE
import com.walletconnect.android.internal.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.android.internal.PAIRING_NOT_ALLOWED_MESSAGE
import com.walletconnect.android.internal.Validator
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.MalformedWalletConnectUri
import com.walletconnect.android.internal.common.exception.PairWithExistingPairingIsNotAllowed
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WalletConnectUri
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.android.pairing.model.PairingJsonRpcMethod
import com.walletconnect.android.pairing.model.PairingParams
import com.walletconnect.android.pairing.model.PairingRpc
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

//Split into PairingProtocolEngine and PairingControllerEngine
internal class PairingEngine(
    private val logger: Logger,
    private val selfMetaData: AppMetaData,
    private val metadataRepository: MetadataStorageRepositoryInterface,
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingRepository: PairingStorageRepositoryInterface
) {
    private var jsonRpcRequestsJob: Job? = null
    private val setOfRegisteredMethods: MutableSet<String> = mutableSetOf()
    private val registeredMethods: String get() = setOfRegisteredMethods.joinToString(",") { it }

    init {
        setOfRegisteredMethods.addAll(listOf(PairingJsonRpcMethod.WC_PAIRING_DELETE, PairingJsonRpcMethod.WC_PAIRING_PING))

        jsonRpcInteractor.isConnectionAvailable
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        resubscribeToPairingFlow()
                    }
                }

                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequestsFlow()
                }
            }.launchIn(scope)
    }

    private val _topicExpiredFlow: MutableSharedFlow<Topic> = MutableSharedFlow()
    val topicExpiredFlow: SharedFlow<Topic> = _topicExpiredFlow.asSharedFlow()

    private val _engineEvent: MutableSharedFlow<EngineDO> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineDO> = _engineEvent.asSharedFlow()

    private val _activePairingTopicFlow: MutableSharedFlow<Topic> = MutableSharedFlow()
    val activePairingTopicFlow: SharedFlow<Topic> = _activePairingTopicFlow.asSharedFlow()

    val internalErrorFlow = MutableSharedFlow<SDKError>()

    val jsonRpcErrorFlow: Flow<SDKError> by lazy {
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.method !in setOfRegisteredMethods }
            .onEach { request ->
                val irnParams = IrnParams(Tags.UNSUPPORTED_METHOD, Ttl(DAY_IN_SECONDS))
                jsonRpcInteractor.respondWithError(request, Invalid.MethodUnsupported(request.method), irnParams)
            }.map { request ->
                SDKError(Exception(Invalid.MethodUnsupported(request.method).message))
            }
    }

    fun create(onFailure: (Throwable) -> Unit): Core.Model.Pairing? {
        val pairingTopic: Topic = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateAndStoreSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptions()
        val registeredMethods = setOfRegisteredMethods.joinToString(",") { it }
        val inactivePairing = Pairing(pairingTopic, relay, symmetricKey, registeredMethods)

        return inactivePairing.runCatching {
            pairingRepository.insertPairing(this)
            metadataRepository.upsertPeerMetadata(this.topic, selfMetaData, AppMetaDataType.SELF)
            jsonRpcInteractor.subscribe(this.topic) { error -> return@subscribe onFailure(error) }

            this.toClient()
        }.onFailure { throwable ->
            crypto.removeKeys(pairingTopic.value)
            pairingRepository.deletePairing(pairingTopic)
            metadataRepository.deleteMetaData(pairingTopic)
            jsonRpcInteractor.unsubscribe(pairingTopic)
            onFailure(throwable)
        }.getOrNull()
    }

    fun pair(uri: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val walletConnectUri: WalletConnectUri = Validator.validateWCUri(uri) ?: return onFailure(MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE))
        val inactivePairing = Pairing(walletConnectUri, registeredMethods)
        val symmetricKey = walletConnectUri.symKey

        try {
            if (isPairingValid(inactivePairing.topic.value)) {
                val pairing = pairingRepository.getPairingOrNullByTopic(inactivePairing.topic)
                if (pairing?.isActive == true) {
                    return onFailure(PairWithExistingPairingIsNotAllowed(PAIRING_NOT_ALLOWED_MESSAGE))
                } else {
                    scope.launch {
                        supervisorScope {
                            _activePairingTopicFlow.emit(inactivePairing.topic)
                        }
                    }
                }
            } else {
                crypto.setKey(symmetricKey, walletConnectUri.topic.value)
                pairingRepository.insertPairing(inactivePairing)
            }

            jsonRpcInteractor.subscribe(topic = inactivePairing.topic, onSuccess = { onSuccess() }, onFailure = { error -> return@subscribe onFailure(error) })
        } catch (e: Exception) {
            crypto.removeKeys(walletConnectUri.topic.value)
            jsonRpcInteractor.unsubscribe(inactivePairing.topic)
            onFailure(e)
        }
    }

    fun disconnect(topic: String, onFailure: (Throwable) -> Unit) {
        if (!isPairingValid(topic)) {
            return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }

        pairingRepository.deletePairing(Topic(topic))
        metadataRepository.deleteMetaData(Topic(topic))
        jsonRpcInteractor.unsubscribe(Topic(topic))

        val deleteParams = PairingParams.DeleteParams(6000, "User disconnected")
        val pairingDelete = PairingRpc.PairingDelete(params = deleteParams)
        val irnParams = IrnParams(Tags.PAIRING_DELETE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pairingDelete,
            onSuccess = { logger.log("Disconnect sent successfully") },
            onFailure = { error ->
                logger.error("Sending session disconnect error: $error")
                onFailure(error)
            }
        )
    }

    fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (isPairingValid(topic)) {
            val pingPayload = PairingRpc.PairingPing(params = PairingParams.PingParams())
            val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
                onSuccess = { onPingSuccess(pingPayload, onSuccess, topic, onFailure) },
                onFailure = { error -> onFailure(error) })
        } else {
            onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${topic}"))
        }
    }

    fun getPairings(): List<Pairing> {
        return pairingRepository.getListOfPairings().filter { pairing -> pairing.isNotExpired() && pairing.isActive }
    }

    fun register(vararg method: String) {
        setOfRegisteredMethods.addAll(method)
    }

    fun activate(topic: String, onFailure: (Throwable) -> Unit) {
        getPairing(topic, onFailure) { pairing -> pairingRepository.activatePairing(pairing.topic) }
    }

    fun updateExpiry(topic: String, expiry: Expiry, onFailure: (Throwable) -> Unit) {
        val pairing: Pairing = pairingRepository.getPairingOrNullByTopic(Topic(topic))?.run {
            this.takeIf { pairing -> pairing.isNotExpired() } ?: return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        } ?: return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))

        val newExpiration = pairing.expiry.seconds + expiry.seconds
        pairingRepository.updateExpiry(Topic(topic), Expiry(newExpiration))
    }

    fun updateMetadata(topic: String, metadata: AppMetaData, metaDataType: AppMetaDataType) {
        metadataRepository.upsertPeerMetadata(Topic(topic), metadata, metaDataType)
    }

    private fun collectJsonRpcRequestsFlow(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PairingParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PairingParams.DeleteParams -> onPairingDelete(request, requestParams)
                    is PairingParams.PingParams -> onPing(request)
                }
            }.launchIn(scope)

    private fun resubscribeToPairingFlow() {
        try {
            val pairingTopics = pairingRepository.getListOfPairings().map { pairing -> pairing.topic.value }
            jsonRpcInteractor.batchSubscribe(pairingTopics) { error -> scope.launch { internalErrorFlow.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { internalErrorFlow.emit(SDKError(e)) }
        }
    }

    private suspend fun onPairingDelete(request: WCRequest, params: PairingParams.DeleteParams) {
        val irnParams = IrnParams(Tags.PAIRING_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
        try {
            if (!isPairingValid(request.topic.value)) {
                jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic("Pairing", request.topic.value), irnParams)
                return
            }

            crypto.removeKeys(request.topic.value)
            jsonRpcInteractor.unsubscribe(request.topic)
            pairingRepository.deletePairing(request.topic)
            metadataRepository.deleteMetaData(request.topic)

            _engineEvent.emit(EngineDO.PairingDelete(request.topic.value, params.message))
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(request, Uncategorized.GenericError("Cannot delete pairing: ${e.message}"), irnParams)
            return
        }
    }

    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }

    private fun onPingSuccess(
        pingPayload: PairingRpc.PairingPing,
        onSuccess: (String) -> Unit,
        topic: String,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            try {
                withTimeout(TimeUnit.SECONDS.toMillis(THIRTY_SECONDS)) {
                    jsonRpcInteractor.peerResponse
                        .filter { response -> response.response.id == pingPayload.id }
                        .collect { response ->
                            when (val result = response.response) {
                                is JsonRpcResponse.JsonRpcResult -> {
                                    cancel()
                                    onSuccess(topic)
                                }

                                is JsonRpcResponse.JsonRpcError -> {
                                    cancel()
                                    onFailure(Throwable(result.errorMessage))
                                }
                            }
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onFailure(e)
            }
        }
    }

    private fun generateTopic(): Topic = Topic(randomBytes(32).bytesToHex())

    private fun getPairing(topic: String, onFailure: (Throwable) -> Unit, onPairing: (pairing: Pairing) -> Unit) {
        pairingRepository.getPairingOrNullByTopic(Topic(topic))?.let { pairing ->
            if (pairing.isNotExpired()) {
                onPairing(pairing)
            } else {
                onFailure(IllegalStateException("Pairing for topic $topic is expired"))
            }
        } ?: onFailure(IllegalStateException("Pairing for topic $topic does not exist"))
    }

    private fun Pairing.isNotExpired(): Boolean = (expiry.seconds > CURRENT_TIME_IN_SECONDS).also { isValid ->
        if (!isValid) {
            scope.launch {
                try {
                    jsonRpcInteractor.unsubscribe(topic = this@isNotExpired.topic)
                    pairingRepository.deletePairing(this@isNotExpired.topic)
                    metadataRepository.deleteMetaData(this@isNotExpired.topic)
                    crypto.removeKeys(this@isNotExpired.topic.value)

                    _topicExpiredFlow.emit(this@isNotExpired.topic)
                } catch (e: Exception) {
                    _topicExpiredFlow.emit(this@isNotExpired.topic)
                }
            }
        }
    }

    private fun isPairingValid(topic: String): Boolean =
        pairingRepository.getPairingOrNullByTopic(Topic(topic))?.let { pairing -> return@let pairing.isNotExpired() } ?: false
}