package com.walletconnect.android.pairing.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.MALFORMED_PAIRING_URI_MESSAGE
import com.walletconnect.android.internal.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.android.internal.Validator
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.ExpiredPairingException
import com.walletconnect.android.internal.common.exception.ExpiredPairingURIException
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.MalformedWalletConnectUri
import com.walletconnect.android.internal.common.exception.NoInternetConnectionException
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
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
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.pairing.PairingStorageRepositoryInterface
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.internal.utils.thirtySeconds
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.android.pairing.model.PairingJsonRpcMethod
import com.walletconnect.android.pairing.model.PairingParams
import com.walletconnect.android.pairing.model.PairingRpc
import com.walletconnect.android.pairing.model.mapper.toCore
import com.walletconnect.android.pairing.model.pairingExpiry
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.android.pulse.domain.InsertTelemetryEventUseCase
import com.walletconnect.android.pulse.domain.SendBatchEventUseCase
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.Trace
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

//Split into PairingProtocolEngine and PairingControllerEngine
internal class PairingEngine(
    private val logger: Logger,
    private val selfMetaData: AppMetaData,
    private val metadataRepository: MetadataStorageRepositoryInterface,
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val pairingRepository: PairingStorageRepositoryInterface,
    private val insertTelemetryEventUseCase: InsertTelemetryEventUseCase,
    private val insertEventUseCase: InsertEventUseCase,
    private val sendBatchEventUseCase: SendBatchEventUseCase,
    private val clientId: String,
    private val userAgent: String
) {
    private var jsonRpcRequestsJob: Job? = null
    private val setOfRegisteredMethods: MutableSet<String> = mutableSetOf()
    private val _isPairingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _storedPairingTopicFlow: MutableSharedFlow<Pair<Topic, MutableList<String>>> = MutableSharedFlow()
    val storedPairingTopicFlow: SharedFlow<Pair<Topic, MutableList<String>>> = _storedPairingTopicFlow.asSharedFlow()
    val internalErrorFlow = MutableSharedFlow<SDKError>()
    private val _checkVerifyKeyFlow: MutableSharedFlow<Unit> = MutableSharedFlow()
    val checkVerifyKeyFlow: SharedFlow<Unit> = _checkVerifyKeyFlow.shareIn(scope, SharingStarted.Lazily, 1)

    private val _engineEvent: MutableSharedFlow<EngineDO> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineDO> =
        merge(_engineEvent, _isPairingStateFlow.map { EngineDO.PairingState(it) })
            .shareIn(scope, SharingStarted.Lazily, 0)


    // TODO: emission of events can be missed since they are emitted potentially before there's a subscriber and the event gets missed by protocols
    init {
        setOfRegisteredMethods.addAll(listOf(PairingJsonRpcMethod.WC_PAIRING_DELETE, PairingJsonRpcMethod.WC_PAIRING_PING))
        resubscribeToPairingTopics()
        pairingsExpiryWatcher()
        isPairingStateWatcher()
        sendEvents()
    }

    val jsonRpcErrorFlow: Flow<SDKError> by lazy {
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.method !in setOfRegisteredMethods }
            .onEach { request ->
                val irnParams = IrnParams(Tags.UNSUPPORTED_METHOD, Ttl(dayInSeconds))
                jsonRpcInteractor.respondWithError(request, Invalid.MethodUnsupported(request.method), irnParams)
            }.map { request ->
                SDKError(Exception(Invalid.MethodUnsupported(request.method).message))
            }
    }

    // TODO: We should either have callbacks or return values, not both. Simplify this to do one or the other. Pairing should be returned if subscription is successful
    fun create(onFailure: (Throwable) -> Unit, methods: String? = null): Core.Model.Pairing? {
        val pairingTopic: Topic = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateAndStoreSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptions()
        val inactivePairing = Pairing(pairingTopic, relay, symmetricKey, Expiry(pairingExpiry), methods)

        return inactivePairing.runCatching {
            logger.log("Creating Pairing")
            pairingRepository.insertPairing(this)
            metadataRepository.upsertPeerMetadata(this.topic, selfMetaData, AppMetaDataType.SELF)
            jsonRpcInteractor.subscribe(
                topic = this.topic,
                onSuccess = { logger.log("Pairing - subscribed on pairing topic: $pairingTopic") },
                onFailure = { error -> logger.error("Pairing - subscribed failure on pairing topic: $pairingTopic, error: $error") }
            )

            this.toCore()
        }.onFailure { throwable ->
            try {
                crypto.removeKeys(pairingTopic.value)
                pairingRepository.deletePairing(pairingTopic)
                metadataRepository.deleteMetaData(pairingTopic)
                jsonRpcInteractor.unsubscribe(pairingTopic)
                logger.error("Pairing - subscribed failure on pairing topic: $pairingTopic, error: $throwable")
                onFailure(throwable)
            } catch (e: Exception) {
                logger.error("Pairing - subscribed failure on pairing topic: $pairingTopic, error: $e")
                onFailure(e)
            }
        }.getOrNull()
    }

    fun pair(uri: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch { _checkVerifyKeyFlow.emit(Unit) }
        val trace: MutableList<String> = mutableListOf()
        trace.add(Trace.Pairing.PAIRING_STARTED).also { logger.log("Pairing started") }
        val walletConnectUri: WalletConnectUri = Validator.validateWCUri(uri) ?: run {
            scope.launch { supervisorScope { insertTelemetryEventUseCase(Props(type = EventType.Error.MALFORMED_PAIRING_URI, properties = Properties(trace = trace))) } }
            return onFailure(MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE))
        }
        trace.add(Trace.Pairing.PAIRING_URI_VALIDATION_SUCCESS)
        val pairing = Pairing(walletConnectUri)
        val pairingTopic = pairing.topic
        val symmetricKey = walletConnectUri.symKey
        try {
            if (walletConnectUri.expiry?.isExpired() == true) {
                scope.launch { supervisorScope { insertTelemetryEventUseCase(Props(type = EventType.Error.PAIRING_URI_EXPIRED, properties = Properties(trace = trace, topic = pairingTopic.value))) } }
                    .also { logger.error("Pairing URI expired: $pairingTopic") }
                return onFailure(ExpiredPairingURIException("Pairing URI expired: $pairingTopic"))
            }
            trace.add(Trace.Pairing.PAIRING_URI_NOT_EXPIRED)
            if (pairingRepository.getPairingOrNullByTopic(pairingTopic) != null) {
                val localPairing = pairingRepository.getPairingOrNullByTopic(pairingTopic)
                trace.add(Trace.Pairing.EXISTING_PAIRING)
                if (!localPairing!!.isNotExpired()) {
                    scope.launch { supervisorScope { insertTelemetryEventUseCase(Props(type = EventType.Error.PAIRING_EXPIRED, properties = Properties(trace = trace, topic = pairingTopic.value))) } }
                        .also { logger.error("Pairing expired: $pairingTopic") }
                    return onFailure(ExpiredPairingException("Pairing expired: ${pairingTopic.value}"))
                }
                trace.add(Trace.Pairing.PAIRING_NOT_EXPIRED)
                scope.launch {
                    supervisorScope {
                        trace.add(Trace.Pairing.EMIT_STORED_PAIRING).also { logger.log("Emitting stored pairing: $pairingTopic") }
                        _storedPairingTopicFlow.emit(Pair(pairingTopic, trace))
                    }
                }
            } else {
                crypto.setKey(symmetricKey, pairingTopic.value)
                pairingRepository.insertPairing(pairing)
                trace.add(Trace.Pairing.STORE_NEW_PAIRING).also { logger.log("Storing a new pairing: $pairingTopic") }
            }
            trace.add(Trace.Pairing.SUBSCRIBING_PAIRING_TOPIC).also { logger.log("Subscribing pairing topic: $pairingTopic") }
            jsonRpcInteractor.subscribe(topic = pairingTopic,
                onSuccess = {
                    trace.add(Trace.Pairing.SUBSCRIBE_PAIRING_TOPIC_SUCCESS).also { logger.log("Subscribe pairing topic success: $pairingTopic") }
                    onSuccess()
                }, onFailure = { error ->
                    scope.launch {
                        supervisorScope {
                            insertTelemetryEventUseCase(Props(type = EventType.Error.PAIRING_SUBSCRIPTION_FAILURE, properties = Properties(trace = trace, topic = pairingTopic.value)))
                        }
                    }.also { logger.error("Subscribe pairing topic error: $pairingTopic, error: $error") }
                    onFailure(error)
                }
            )
        } catch (e: Exception) {
            logger.error("Subscribe pairing topic error: $pairingTopic, error: $e")
            if (e is NoRelayConnectionException)
                scope.launch { supervisorScope { insertTelemetryEventUseCase(Props(type = EventType.Error.NO_WSS_CONNECTION, properties = Properties(trace = trace, topic = pairingTopic.value))) } }
            if (e is NoInternetConnectionException)
                scope.launch {
                    supervisorScope {
                        insertTelemetryEventUseCase(
                            Props(
                                type = EventType.Error.NO_INTERNET_CONNECTION,
                                properties = Properties(trace = trace, topic = pairingTopic.value)
                            )
                        )
                    }
                }
            runCatching { crypto.removeKeys(pairingTopic.value) }.onFailure { logger.error("Remove keys error: $pairingTopic, error: $it") }
            jsonRpcInteractor.unsubscribe(pairingTopic)
            onFailure(e)
        }
    }

    @Deprecated(message = "Disconnect method has been deprecated. It will be removed soon. Pairing will disconnect automatically internally.")
    fun disconnect(topic: String, onFailure: (Throwable) -> Unit) {
        if (!isPairingValid(topic)) {
            return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }

        val pairing = pairingRepository.getPairingOrNullByTopic(Topic(topic))
        val deleteParams = PairingParams.DeleteParams(6000, "User disconnected")
        val pairingDelete = PairingRpc.PairingDelete(params = deleteParams)
        val irnParams = IrnParams(Tags.PAIRING_DELETE, Ttl(dayInSeconds))
        logger.log("Sending Pairing disconnect")
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pairingDelete,
            onSuccess = {
                scope.launch {
                    supervisorScope {
                        logger.log("Pairing disconnect sent successfully")
                        pairingRepository.deletePairing(Topic(topic))
                        metadataRepository.deleteMetaData(Topic(topic))
                        jsonRpcInteractor.unsubscribe(Topic(topic))
                    }
                }
            },
            onFailure = { error ->
                logger.error("Sending session disconnect error: $error")
                onFailure(error)
            }
        )
    }

    @Deprecated(message = "Ping method has been deprecated. It will be removed soon.")
    fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (isPairingValid(topic)) {
            val pingPayload = PairingRpc.PairingPing(params = PairingParams.PingParams())
            val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(thirtySeconds))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
                onSuccess = { onPingSuccess(pingPayload, onSuccess, topic, onFailure) },
                onFailure = { error -> onFailure(error) })
        } else {
            onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${topic}"))
        }
    }

    fun getPairings(): List<Pairing> = runBlocking { pairingRepository.getListOfPairings().filter { pairing -> pairing.isNotExpired() } }

    fun register(vararg method: String) {
        setOfRegisteredMethods.addAll(method)
    }

    fun getPairingByTopic(topic: Topic): Pairing? = pairingRepository.getPairingOrNullByTopic(topic)?.takeIf { pairing -> pairing.isNotExpired() }

    fun setRequestReceived(topic: String, onFailure: (Throwable) -> Unit) {
        getPairing(topic, onFailure) { pairing -> pairingRepository.setRequestReceived(pairing.topic) }
    }

    fun updateMetadata(topic: String, metadata: AppMetaData, metaDataType: AppMetaDataType) {
        metadataRepository.upsertPeerMetadata(Topic(topic), metadata, metaDataType)
    }

    fun deleteAndUnsubscribePairing(topic: String) {
        jsonRpcInteractor.unsubscribe(Topic(topic))
        pairingRepository.deletePairing(Topic(topic))
    }

    private fun sendEvents() {
        scope.launch {
            supervisorScope {
                try {
                    insertEventUseCase(Props(event = EventType.INIT, properties = Properties(clientId = clientId, userAgent = userAgent)))
                    sendBatchEventUseCase()
                } catch (e: Exception) {
                    logger.error("Error when sending events: $e")
                }
            }
        }
    }

    private fun resubscribeToPairingTopics() {
        jsonRpcInteractor.onResubscribe
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        sendBatchSubscribeForPairings()
                    }
                }

                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequestsFlow()
                }
            }.launchIn(scope)
    }

    private suspend fun sendBatchSubscribeForPairings() {
        try {
            val pairingTopics = pairingRepository.getListOfPairings().filter { pairing -> pairing.isNotExpired() }.map { pairing -> pairing.topic.value }
            jsonRpcInteractor.batchSubscribe(pairingTopics) { error -> scope.launch { internalErrorFlow.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { internalErrorFlow.emit(SDKError(e)) }
        }
    }

    private fun pairingsExpiryWatcher() {
        repeatableFlow(WATCHER_INTERVAL)
            .onEach {
                try {
                    pairingRepository.getListOfPairings()
                        .onEach { pairing -> pairing.isNotExpired() }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }.launchIn(scope)
    }

    private fun isPairingStateWatcher() {
        repeatableFlow(WATCHER_INTERVAL)
            .onEach {
                try {
                    val inactivePairings = pairingRepository.getListOfPairingsWithoutRequestReceived()
                    if (inactivePairings.isNotEmpty()) {
                        _isPairingStateFlow.compareAndSet(expect = false, update = true)
                    } else {
                        _isPairingStateFlow.compareAndSet(expect = true, update = false)
                    }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }.launchIn(scope)
    }

    private fun repeatableFlow(interval: Long) = flow {
        while (true) {
            emit(Unit)
            delay(interval)
        }
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

    @Deprecated(message = "This method has been deprecated. It will be removed soon.")
    private suspend fun onPairingDelete(request: WCRequest, params: PairingParams.DeleteParams) {
        val irnParams = IrnParams(Tags.PAIRING_DELETE_RESPONSE, Ttl(dayInSeconds))
        try {
            val pairing = pairingRepository.getPairingOrNullByTopic(request.topic)
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

    @Deprecated(message = "Ping method has been deprecated. It will be removed soon.")
    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.PAIRING_PING, Ttl(thirtySeconds))
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }

    @Deprecated(message = "Ping method has been deprecated. It will be removed soon.")
    private fun onPingSuccess(
        pingPayload: PairingRpc.PairingPing,
        onSuccess: (String) -> Unit,
        topic: String,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            try {
                withTimeout(TimeUnit.SECONDS.toMillis(thirtySeconds)) {
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

    private fun Pairing.isNotExpired(): Boolean = (expiry.seconds > currentTimeInSeconds).also { isValid ->
        if (!isValid) {
            scope.launch {
                try {
                    jsonRpcInteractor.unsubscribe(topic = this@isNotExpired.topic)
                    pairingRepository.deletePairing(this@isNotExpired.topic)
                    metadataRepository.deleteMetaData(this@isNotExpired.topic)
                    crypto.removeKeys(this@isNotExpired.topic.value)
                } catch (e: Exception) {
                    logger.error("Error when deleting pairing: $e")
                }
            }
        }
    }

    @Deprecated(message = "This method has been deprecated. It will be removed soon.")
    private fun isPairingValid(topic: String): Boolean =
        pairingRepository.getPairingOrNullByTopic(Topic(topic))?.let { pairing -> return@let pairing.isNotExpired() } ?: false

    companion object {
        private const val WATCHER_INTERVAL = 30000L //30s
    }
}