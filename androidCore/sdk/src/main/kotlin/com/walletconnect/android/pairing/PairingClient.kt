@file:JvmSynthetic

package com.walletconnect.android.pairing

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
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
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import com.walletconnect.util.randomBytes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.dsl.module

internal object PairingClient : PairingInterface {
    private val selfMetaData: AppMetaData by lazy { load() }
    private val setOfRegisteredMethods: MutableSet<String> = mutableSetOf()
    private val registeredMethods: String
        get() = setOfRegisteredMethods.joinToString(",") { it }
    private val pairingEvent = MutableSharedFlow<PairingDO>()
    private val _topicExpiredFlow: MutableSharedFlow<Topic> = MutableSharedFlow()
    override val topicExpiredFlow: SharedFlow<Topic> = _topicExpiredFlow.asSharedFlow()
    private val pairingRepository: PairingStorageRepositoryInterface by lazy { load() }
    private val metadataRepository: MetadataStorageRepositoryInterface by lazy { load() }
    private val crypto: KeyManagementRepository by lazy { load() }
    private val jsonRpcInteractor: JsonRpcInteractorInterface by lazy { load() }
    private val logger: Logger by lazy { wcKoinApp.koin.get() }
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
                                    jsonRpcInteractor.subscribe(pairingTopic)
                                } catch (_: NoRelayConnectionException) {}
                            }
                    }
                }
            }
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
    override val findWrongMethodsFlow: Flow<InternalError> by lazy {
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.method !in setOfRegisteredMethods }
            .onEach {
                val irnParams = IrnParams(Tags.UNSUPPORTED_METHOD, Ttl(DAY_IN_SECONDS))
                jsonRpcInteractor.respondWithError(it, Invalid.MethodUnsupported(it.method), irnParams)
            }.map {
                InternalError(Exception(Invalid.MethodUnsupported(it.method).message))
            }
    }

    fun initialize(metaData: Core.Model.AppMetaData) {
        wcKoinApp.modules(module {
            single {
                AppMetaData(metaData.name, metaData.description, metaData.url, metaData.icons, Redirect(metaData.redirect))
            }
        })
    }

    fun setDelegate(delegate: PairingInterface.Delegate) {
        pairingEvent.onEach { event ->
            when (event) {
                is PairingDO.PairingDelete -> delegate.onPairingDelete(event.toClient())
            }
        }.launchIn(scope)
    }

    override fun ping(ping: Core.Params.Ping, pairingPing: Core.Listeners.PairingPing?) {
        if (isPairingValid(ping.topic)) {
            val pingPayload = PairingRpc.PairingPing(id = generateId(), params = PairingParams.PingParams())
            val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(ping.topic), irnParams, pingPayload,
                onSuccess = {
                    scope.launch {
                        try {
                            withTimeout(THIRTY_SECONDS) {
                                jsonRpcInteractor.peerResponse
                                    .filter { response -> response.response.id == pingPayload.id }
                                    .collect { response ->
                                        when (val result = response.response) {
                                            is JsonRpcResponse.JsonRpcResult -> {
                                                cancel()
                                                pairingPing?.onSuccess(Core.Model.Ping.Success(ping.topic))
                                            }
                                            is JsonRpcResponse.JsonRpcError -> {
                                                cancel()
                                                pairingPing?.onError(Core.Model.Ping.Error(Throwable(result.errorMessage)))
                                            }
                                        }
                                    }
                            }
                        } catch (e: TimeoutCancellationException) {
                            pairingPing?.onError(Core.Model.Ping.Error(e))
                        }
                    }
                },
                onFailure = { error ->
                    pairingPing?.onError(Core.Model.Ping.Error(error))
                })
        } else {
            pairingPing?.onError(Core.Model.Ping.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${ping.topic}")))
        }
    }

    override fun create(onError: (Core.Model.Error) -> Unit): Core.Model.Pairing? {
        val pairingTopic: Topic = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateAndStoreSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptions()
        val registeredMethods = setOfRegisteredMethods.joinToString(",") { it }
        val inactivePairing = Pairing(pairingTopic, relay, symmetricKey, registeredMethods)

        return inactivePairing.runCatching {
            pairingRepository.insertPairing(this)
            metadataRepository.upsertPairingPeerMetadata(pairingTopic, selfMetaData, AppMetaDataType.SELF)
            jsonRpcInteractor.subscribe(pairingTopic)

            this.toClient()
        }.onFailure {
            crypto.removeKeys(pairingTopic.value)
            pairingRepository.deletePairing(pairingTopic)
            metadataRepository.deleteMetaData(pairingTopic)
            jsonRpcInteractor.unsubscribe(pairingTopic)
            onError(Core.Model.Error(it))
        }.getOrNull()
    }

    override fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit) {
        scope.launch(Dispatchers.IO) {
            awaitConnection({
                val walletConnectUri: WalletConnectUri = Validator.validateWCUri(pair.uri) ?: return@awaitConnection onError(
                    Core.Model.Error(
                        MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)
                    )
                )

                if (isPairingValid(walletConnectUri.topic.value)) {
                    return@awaitConnection onError(Core.Model.Error(PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)))
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
                    jsonRpcInteractor.subscribe(activePairing.topic)
                } catch (e: Exception) {
                    crypto.removeKeys(walletConnectUri.topic.value)
                    jsonRpcInteractor.unsubscribe(activePairing.topic)
                    onError(Core.Model.Error(e))
                }
            }, { throwable ->
                logger.error(throwable)
                onError(Core.Model.Error(throwable))
            })
        }
    }

    override fun getPairings(): List<Core.Model.Pairing> {
        return pairingRepository.getListOfPairings().filter { pairing ->
            pairing.isValid() && pairing.isActive
        }.map { pairing ->
            pairing.toClient()
        }
    }

    override fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit) {
        if (!isPairingValid(topic)) {
            return onError(Core.Model.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")))
        }

        pairingRepository.deletePairing(Topic(topic))
        metadataRepository.deleteMetaData(Topic(topic))
        jsonRpcInteractor.unsubscribe(Topic(topic))

        val deleteParams = PairingParams.DeleteParams(6000, "User disconnected")
        val pairingDelete = PairingRpc.PairingDelete(id = generateId(), params = deleteParams)
        val irnParams = IrnParams(Tags.PAIRING_DELETE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pairingDelete,
            onSuccess = { logger.log("Disconnect sent successfully") },
            onFailure = { error -> logger.error("Sending session disconnect error: $error") }
        )
    }

    override fun activate(topic: String, onError: (Core.Model.Error) -> Unit) {
        pairingRepository.getPairingOrNullByTopic(Topic(topic))?.let { pairing ->
            if (pairing.isValid()) {
                pairingRepository.activatePairing(pairing.topic)
            } else {
                onError(Core.Model.Error(IllegalStateException("Pairing for topic $topic is expired")))
            }
        } ?: onError(Core.Model.Error(IllegalStateException("Pairing for topic $topic does not exist")))
    }

    override fun updateExpiry(topic: String, expiry: Expiry, onError: (Core.Model.Error) -> Unit) {
        val pairing: Pairing = pairingRepository.getPairingOrNullByTopic(Topic(topic))?.run {
            this.takeIf { it.isValid() } ?: return onError(Core.Model.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")))
        } ?: return onError(Core.Model.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")))

        val newExpiration = pairing.expiry.seconds + expiry.seconds
        pairingRepository.updateExpiry(Topic(topic), Expiry(newExpiration))
    }

    override fun updateMetadata(topic: String, metadata: Core.Model.AppMetaData, metaDataType: AppMetaDataType, onError: (Core.Model.Error) -> Unit) {
        try {
            metadataRepository.upsertPairingPeerMetadata(Topic(topic), metadata.toAppMetaData(), metaDataType)
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    override fun register(vararg method: String) {
        setOfRegisteredMethods.addAll(method)
    }

    private suspend fun onPairingDelete(request: WCRequest, params: PairingParams.DeleteParams) {
        if (!isPairingValid(request.topic.value)) {
            val irnParams = IrnParams(Tags.PAIRING_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
            jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic("Pairing", request.topic.value), irnParams)
            return
        }

        crypto.removeKeys(request.topic.value)
        jsonRpcInteractor.unsubscribe(request.topic)
        pairingRepository.deletePairing(request.topic)
        metadataRepository.deleteMetaData(request.topic)

        pairingEvent.emit(PairingDO.PairingDelete(request.topic.value, params.message))
    }

    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }

    private inline fun <reified T> load(): T {
        return wcKoinApp.koin.getOrNull<T>(T::class).also { temp ->
            if (temp != null) {
                scope.launch {
                    supervisorScope { resubscribeToPairingFlow.launchIn(this) }
                    supervisorScope { collectJsonRpcRequestsFlow.launchIn(this) }
                    supervisorScope { findWrongMethodsFlow.launchIn(this) }
                }
            }
        } ?: throw IllegalStateException("Core cannot be initialized by itself")
    }

    private fun isPairingValid(topic: String): Boolean = pairingRepository.getPairingOrNullByTopic(Topic(topic)).let { pairing ->
        if (pairing == null) {
            return@let false
        } else {
            return@let pairing.isValid()
        }
    }

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

    private fun generateTopic(): Topic = Topic(randomBytes(32).bytesToHex())

    private suspend fun awaitConnection(onConnection: () -> Unit, errorLambda: (Throwable) -> Unit = {}) {
        try {
            withTimeout(2000) {
                while (true) {
                    if (CoreClient.Relay.isConnectionAvailable.value) {
                        onConnection()
                        return@withTimeout
                    }
                    delay(100)
                }
            }
        } catch (e: Exception) {
            errorLambda(e)
        }

    }
}