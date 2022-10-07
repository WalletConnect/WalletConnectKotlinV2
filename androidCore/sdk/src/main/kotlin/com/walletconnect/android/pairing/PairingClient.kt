@file:JvmSynthetic

package com.walletconnect.android.pairing

import android.database.sqlite.SQLiteException
import com.walletconnect.android.Core
import com.walletconnect.android.common.*
import com.walletconnect.android.common.crypto.KeyManagementRepository
import com.walletconnect.android.common.model.*
import com.walletconnect.android.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.exception.CannotFindSequenceForTopic
import com.walletconnect.android.exception.MalformedWalletConnectUri
import com.walletconnect.android.exception.PairWithExistingPairingIsNotAllowed
import com.walletconnect.android.internal.*
import com.walletconnect.android.utils.isSequenceValid
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import com.walletconnect.util.randomBytes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal object PairingClient : PairingInterface {
    private val methodsToCallbacks: MutableMap<String, (topic: String, request: WCRequest) -> Unit> = mutableMapOf()

    private val pairingEvent = MutableSharedFlow<Unit>()

    private lateinit var _selfMetaData: Core.Model.AppMetaData
    override val selfMetaData: Core.Model.AppMetaData
        get() = _selfMetaData

    private val pairingRepository: PairingStorageRepositoryInterface
        get() = load()
    private val metadataRepository: MetadataStorageRepositoryInterface
        get() = load()
    private val crypto: KeyManagementRepository
        get() = load()
    private val jsonRpcInteractor: JsonRpcInteractorInterface
        get() = load()
    private val resubscribeToPairingJob by lazy {
        jsonRpcInteractor.isConnectionAvailable
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) {
                        val (listOfExpiredPairing, listOfValidPairing) =
                            pairingRepository.getListOfPairings().partition { pairing -> !pairing.expiry.isSequenceValid() }

                        listOfExpiredPairing
                            .map { pairing -> pairing.topic }
                            .onEach { pairingTopic ->
                                jsonRpcInteractor.unsubscribe(pairingTopic)
                                crypto.removeKeys(pairingTopic.value)
                                pairingRepository.deletePairing(pairingTopic)
                                metadataRepository.deleteMetaData(pairingTopic)
                            }

                        listOfValidPairing
                            .map { pairing -> pairing.topic }
                            .onEach { pairingTopic -> jsonRpcInteractor.subscribe(pairingTopic) }
                    }
                }
            }
            .launchIn(scope)
    }
    private val collectJsonRpcRequestsJob by lazy {
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PairingParams.SessionProposeParams || request.params is PairingParams.DeleteParams || request.params is PairingParams.PingParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PairingParams.SessionProposeParams -> onSessionPropose(request, requestParams)
                    is PairingParams.DeleteParams -> onPairingDelete(request, requestParams)
                    is PairingParams.PingParams -> onPing(request)
                }
            }.launchIn(scope)
    }

    init {
        // Match it with signEngine init {}
    }

    fun initialize(metaData: Core.Model.AppMetaData) {
        _selfMetaData = metaData
    }

    fun setDelegate(delegate: PairingInterface.PairingDelegate) {
        pairingEvent.onEach { event ->

            delegate.onSessionProposal(event)
            delegate.onSessionDelete(event)
        }.launchIn(scope)
    }

    override fun ping(ping: Core.Params.Ping, sessionPing: Core.Listeners.SessionPing?) {
        if (pairingRepository.isPairingValid(Topic(ping.topic))) {
            val pingPayload = PairingRpc.PairingPing(id = generateId(), params = PairingParams.PingParams())
            val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))
            jsonRpcInteractor.publishJsonRpcRequests(Topic(ping.topic), irnParams, pingPayload, onSuccess = {
                scope.launch {
                    try {
                        withTimeout(THIRTY_SECONDS) {
                            collectResponse(pingPayload.id) { result ->
                                cancel()
                                result.fold(
                                    onSuccess = {
                                        sessionPing?.onSuccess(Core.Model.Ping.Success(ping.topic))
                                    },
                                    onFailure = { error ->
                                        sessionPing?.onError(Core.Model.Ping.Error(error))
                                    })
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        sessionPing?.onError(Core.Model.Ping.Error(e))
                    }
                }
            },
                onFailure = { error ->
                    sessionPing?.onError(Core.Model.Ping.Error(error))
                })
        } else {
            sessionPing?.onError(Core.Model.Ping.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${ping.topic}")))
        }
    }

    override fun create(onPairingCreated: (String) -> Unit, onError: (Core.Model.Error) -> Unit) {
        val pairingTopic: Topic = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateAndStoreSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptions()
        val walletConnectUri = WalletConnectUri(pairingTopic, symmetricKey, relay)
        val inactivePairing = Pairing(walletConnectUri)

        try {
            pairingRepository.insertPairing(inactivePairing)
            jsonRpcInteractor.subscribe(pairingTopic)
            onPairingCreated(pairingTopic.value)
        } catch (e: Exception) {
            crypto.removeKeys(pairingTopic.value)
            pairingRepository.deletePairing(pairingTopic)
            metadataRepository.deleteMetaData(pairingTopic)
            jsonRpcInteractor.unsubscribe(pairingTopic)

            onError(Core.Model.Error(e))
        }
    }

    override fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit) {
        val walletConnectUri: WalletConnectUri = Validator.validateWCUri(pair.uri) ?: return onError(Core.Model.Error(MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)))

        if (pairingRepository.isPairingValid(walletConnectUri.topic)) {
            return onError(Core.Model.Error(PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)))
        }

        val activePairing = Pairing(walletConnectUri)
        val symmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)

        try {
            pairingRepository.insertPairing(activePairing)
            jsonRpcInteractor.subscribe(activePairing.topic)
        } catch (e: SQLiteException) {
            crypto.removeKeys(walletConnectUri.topic.value)
            jsonRpcInteractor.unsubscribe(activePairing.topic)
            onError(Core.Model.Error(e))
        }
    }

    override fun getPairings(): List<Pairing> {
        return pairingRepository.getListOfPairings()
            .filter { pairing -> pairing.expiry.isSequenceValid() }
    }

    // TODO: add parameter to unsubscribe and publish SessionDelete from the RelayClient
    override fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit) {
        if (!pairingRepository.isPairingValid(Topic(topic))) {
            return onError(Core.Model.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")))
        }

        pairingRepository.deletePairing(Topic(topic))
        metadataRepository.deleteMetaData(Topic(topic))
        jsonRpcInteractor.unsubscribe(Topic(topic))
        // TODO: Move PeerError related to either internal directory or common
        val deleteParams = PairingParams.DeleteParams(6000, "User disconnected")
        val pairingDelete = PairingRpc.PairingDelete(id = generateId(), params = deleteParams)
        val irnParams = IrnParams(Tags.PAIRING_DELETE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequests(Topic(topic), irnParams, pairingDelete,
            onSuccess = { /*TODO: add logger*/ },
            onFailure = { error -> /*TODO: add logger*/ }
        )
    }

    override fun activate(topic: String, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateExpiry(topic: String, expiry: Expiry, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateMetadata(topic: String, metadata: AppMetaData, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    //    @Throws(MethodAlreadyRegistered::class)
    override fun register(method: String, onMethod: (topic: String, request: WCRequest) -> Unit) {
//        if (methodsToCallbacks.containsKey(method)) throw MethodAlreadyRegistered("Method: $method already registered")

        methodsToCallbacks[method] = onMethod
    }

    private fun generateTopic(): Topic = Topic(randomBytes(32).bytesToHex())

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit = {}) {
        jsonRpcInteractor.peerResponse
            .filter { response -> response.response.id == id }
            .collect { response ->
                when (val result = response.response) {
                    is JsonRpcResponse.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponse.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
                }
            }
    }

    private inline fun <reified T> load(): T {
        return wcKoinApp.koin.getOrNull<T>(T::class).also { temp ->
            if (temp != null && !resubscribeToPairingJob.isActive) {
                scope.launch {
                    supervisorScope { resubscribeToPairingJob.join() }
                    supervisorScope { collectJsonRpcRequestsJob.join() }
                }
            }
        } ?: throw IllegalStateException("SDK has not been initialized")
    }

    private fun onSessionPropose(request: WCRequest, payloadParams: PairingParams.SessionProposeParams) {
        Validator.validateProposalNamespace(payloadParams.namespaces) { error ->
            val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
            jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        sessionProposalRequest[payloadParams.proposer.publicKey] = request
        scope.launch { _engineEvent.emit(payloadParams.toEngineDO()) }
    }

    private fun onPairingDelete(request: WCRequest, params: PairingParams.DeleteParams) {
        if (!pairingRepository.isPairingValid(request.topic)) {
            val irnParams = IrnParams(Tags.PAIRING_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
            jsonRpcInteractor.respondWithError(request, PeerError.Uncategorized.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value), irnParams)
            return
        }

        crypto.removeKeys(request.topic.value)
        jsonRpcInteractor.unsubscribe(request.topic)
        pairingRepository.deletePairing(request.topic)
        metadataRepository.deleteMetaData(request.topic)

        scope.launch { _engineEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.message)) }
    }

    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }
}