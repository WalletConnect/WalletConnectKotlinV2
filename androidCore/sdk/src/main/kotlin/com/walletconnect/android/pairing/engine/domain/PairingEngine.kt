package com.walletconnect.android.pairing.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.MALFORMED_PAIRING_URI_MESSAGE
import com.walletconnect.android.internal.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.android.internal.PAIRING_NOW_ALLOWED_MESSAGE
import com.walletconnect.android.internal.Validator
import com.walletconnect.android.internal.common.*
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.*
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.android.pairing.model.PairingParams
import com.walletconnect.android.pairing.model.PairingRpc
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import com.walletconnect.util.randomBytes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class PairingEngine {
    private var resubscribeToPairingsJob: Job? = null
    private var jsonRpcRequestsJob: Job? = null

    private val logger: Logger by lazy { wcKoinApp.koin.get() }
    private val selfMetaData: AppMetaData by lazy { load() }
    private val pairingRepository: PairingStorageRepositoryInterface by lazy { load() }
    private val metadataRepository: MetadataStorageRepositoryInterface by lazy { load() }
    private val crypto: KeyManagementRepository by lazy { load() }
    private val jsonRpcInteractor: JsonRpcInteractorInterface by lazy { load() }

    private val setOfRegisteredMethods: MutableSet<String> = mutableSetOf()
    private val registeredMethods: String get() = setOfRegisteredMethods.joinToString(",") { it }

    private val _topicExpiredFlow: MutableSharedFlow<Topic> = MutableSharedFlow()
    val topicExpiredFlow: SharedFlow<Topic> = _topicExpiredFlow.asSharedFlow()

    private val _engineEvent: MutableSharedFlow<EngineDO> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineDO> = _engineEvent.asSharedFlow()

    val internalErrorFlow = MutableSharedFlow<InternalError>()

    val jsonRpcErrorFlow: Flow<InternalError> by lazy {
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.method !in setOfRegisteredMethods }
            .onEach { request ->
                val irnParams = IrnParams(Tags.UNSUPPORTED_METHOD, Ttl(DAY_IN_SECONDS))
                jsonRpcInteractor.respondWithError(request, Invalid.MethodUnsupported(request.method), irnParams)
            }.map { request ->
                InternalError(Exception(Invalid.MethodUnsupported(request.method).message))
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
            metadataRepository.upsertPairingPeerMetadata(pairingTopic, selfMetaData, AppMetaDataType.SELF)
            jsonRpcInteractor.subscribe(pairingTopic) { error -> return@subscribe onFailure(error) }

            this.toClient()
        }.onFailure { throwable ->
            crypto.removeKeys(pairingTopic.value)
            pairingRepository.deletePairing(pairingTopic)
            metadataRepository.deleteMetaData(pairingTopic)
            jsonRpcInteractor.unsubscribe(pairingTopic)
            onFailure(throwable)
        }.getOrNull()
    }

    fun pair(uri: String, onFailure: (Throwable) -> Unit) {
        val walletConnectUri: WalletConnectUri =
            Validator.validateWCUri(uri) ?: return onFailure(MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE))

        if (isPairingValid(walletConnectUri.topic.value)) {
            return onFailure(PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE))
        }

        // TODO: Will add back in after initial release of Auth
//        if (Validator.doesNotContainRegisteredMethods(walletConnectUri.registeredMethods, setOfRegisteredMethods)) {
//            val deleteParams = PairingParams.DeleteParams(10001, "Methods Unsupported")
//            val pairingDelete = PairingRpc.PairingDelete(id = generateId(), params = deleteParams)
//            val irnParams = IrnParams(Tags.PAIRING_DELETE, Ttl(DAY_IN_SECONDS))
//
//            return jsonRpcInteractor.publishJsonRpcRequests(walletConnectUri.topic, irnParams, pairingDelete,
//                onSuccess = {
//                    onError(Core.Model.Error(IllegalArgumentException("Peer Required RPC  Methods Missing")))
//                },
//                onFailure = {
//                    onError(Core.Model.Error(it))
//                }
//            )
//        }

        val activePairing = Pairing(walletConnectUri, registeredMethods)
        val symmetricKey = walletConnectUri.symKey
        crypto.setKey(symmetricKey, walletConnectUri.topic.value)

        try {
            pairingRepository.insertPairing(activePairing)
            jsonRpcInteractor.subscribe(activePairing.topic) { error -> return@subscribe onFailure(error) }
        } catch (e: Exception) {
            crypto.removeKeys(walletConnectUri.topic.value)
            jsonRpcInteractor.unsubscribe(activePairing.topic)
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
        val pairingDelete = PairingRpc.PairingDelete(id = generateId(), params = deleteParams)
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
            val pingPayload = PairingRpc.PairingPing(id = generateId(), params = PairingParams.PingParams())
            val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
                onSuccess = { onPingSuccess(pingPayload, onSuccess, topic, onFailure) },
                onFailure = { error -> onFailure(error) })
        } else {
            onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${topic}"))
        }
    }

    fun getPairings(): List<Pairing> {
        return pairingRepository.getListOfPairings().filter { pairing -> pairing.isValid() && pairing.isActive }
    }

    fun register(vararg method: String) {
        setOfRegisteredMethods.addAll(method)
    }

    fun activate(topic: String, onFailure: (Throwable) -> Unit) {
        pairingRepository.getPairingOrNullByTopic(Topic(topic))?.let { pairing ->
            if (pairing.isValid()) {
                pairingRepository.activatePairing(pairing.topic)
            } else {
                onFailure(IllegalStateException("Pairing for topic $topic is expired"))
            }
        } ?: onFailure(IllegalStateException("Pairing for topic $topic does not exist"))
    }

    fun updateExpiry(topic: String, expiry: Expiry, onFailure: (Throwable) -> Unit) {
        val pairing: Pairing = pairingRepository.getPairingOrNullByTopic(Topic(topic))?.run {
            this.takeIf { pairing -> pairing.isValid() } ?: return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        } ?: return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))

        val newExpiration = pairing.expiry.seconds + expiry.seconds
        pairingRepository.updateExpiry(Topic(topic), Expiry(newExpiration))
    }

    fun updateMetadata(topic: String, metadata: AppMetaData, metaDataType: AppMetaDataType) {
        metadataRepository.upsertPairingPeerMetadata(Topic(topic), metadata, metaDataType)
    }

    private inline fun <reified T> load(): T {
        return wcKoinApp.koin.getOrNull<T>(T::class).also { temp ->
            if (temp != null) {
                scope.launch {
                    if (resubscribeToPairingsJob == null) {
                        supervisorScope { resubscribeToPairingsJob = resubscribeToPairingFlow.launchIn(this) }
                    }
                    if (jsonRpcRequestsJob == null) {
                        supervisorScope { jsonRpcRequestsJob = collectJsonRpcRequestsFlow.launchIn(this) }
                    }
                }
            }
        } ?: throw IllegalStateException("Core cannot be initialized by itself")
    }

    private val collectJsonRpcRequestsFlow: Flow<WCRequest> by lazy {
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PairingParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PairingParams.DeleteParams -> onPairingDelete(request, requestParams)
                    is PairingParams.PingParams -> onPing(request)
                }
            }
    }

    private val resubscribeToPairingFlow: Flow<Boolean> by lazy {
        jsonRpcInteractor.isConnectionAvailable
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) {
                        pairingRepository.getListOfPairings()
                            .map { pairing -> pairing.topic }
                            .onEach { pairingTopic ->
                                try {
                                    jsonRpcInteractor.subscribe(pairingTopic) { error -> scope.launch { internalErrorFlow.emit(InternalError(error)) } }
                                } catch (e: Exception) {
                                    scope.launch {
                                        internalErrorFlow.emit(InternalError(e))
                                    }
                                }
                            }
                    }
                }
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
                withTimeout(THIRTY_SECONDS) {
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

    private fun isPairingValid(topic: String): Boolean = pairingRepository.getPairingOrNullByTopic(Topic(topic)).let { pairing ->
        if (pairing == null) {
            return@let false
        } else {
            return@let pairing.isValid()
        }
    }

    private fun generateTopic(): Topic = Topic(randomBytes(32).bytesToHex())

    private fun Pairing.isValid(): Boolean = (expiry.seconds > CURRENT_TIME_IN_SECONDS).also { isPairingValid ->
        if (!isPairingValid) {
            scope.launch {
                jsonRpcInteractor.unsubscribe(topic = this@isValid.topic)
                pairingRepository.deletePairing(this@isValid.topic)
                metadataRepository.deleteMetaData(this@isValid.topic)
                crypto.removeKeys(this@isValid.topic.value)

                _topicExpiredFlow.emit(this@isValid.topic)
            }
        }
    }
}