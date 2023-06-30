@file:JvmSynthetic

package com.walletconnect.push.dapp.engine

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.domain.ExtractPushConfigUseCase
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.dapp.data.CastRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope

internal class PushDappEngine(
    private val selfAppMetaData: AppMetaData,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingHandler: PairingControllerInterface,
    private val extractPushConfigUseCase: ExtractPushConfigUseCase,
    private val crypto: KeyManagementRepository,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val castRepository: CastRepository,
    private val logger: Logger,
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_PUSH_REQUEST,
            JsonRpcMethod.WC_PUSH_MESSAGE
        )
    }

    fun setup() {
//        jsonRpcInteractor.isConnectionAvailable
//            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
//            .filter { isAvailable: Boolean -> isAvailable }
//            .onEach {
//                supervisorScope {
//                    launch(Dispatchers.IO) {
//                        resubscribeToSubscriptions()
//                    }
//                }
//
//                castRepository.retryRegistration()
//
//                if (jsonRpcRequestsJob == null) {
//                    jsonRpcRequestsJob = collectJsonRpcRequests()
//                }
//
//                if (jsonRpcResponsesJob == null) {
//                    jsonRpcResponsesJob = collectJsonRpcResponses()
//                }
//
//                if (internalErrorsJob == null) {
//                    internalErrorsJob = collectInternalErrors()
//                }
//            }
//            .launchIn(scope)
    }

    suspend fun propose(
        account: String,
        scope: List<String>,
        pairingTopic: String,
        onSuccess: (Long) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
//        val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
//        val proposeParams = PushParams.ProposeParams(selfPublicKey.keyAsHex, selfAppMetaData, account, scope)
//        val propose = PushRpc.PushPropose(params = proposeParams)
//        val irnParams = IrnParams(Tags.PUSH_PROPOSE, Ttl(DAY_IN_SECONDS), true)
//        val responseTopic = sha256(selfPublicKey.keyAsBytes)
//
//        jsonRpcInteractor.subscribe(Topic(responseTopic)) { error -> return@subscribe onFailure(error) }
//
//        jsonRpcInteractor.publishJsonRpcRequest(Topic(pairingTopic), irnParams, propose,
//            onSuccess = {
//                logger.log("Push Request sent successfully")
//                onSuccess(propose.id)
//            },
//            onFailure = { error ->
//                logger.error("Failed to send a push request: $error")
//                onFailure(error)
//            }
//        )
    }

    suspend fun notify(
        pushTopic: String,
        message: EngineDO.PushMessage,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
//        val messageParams = PushParams.MessageParams(message.title, message.body, message.icon, message.url, message.type)
//        val request = PushRpc.PushMessage(params = messageParams)
//        val irnParams = IrnParams(Tags.PUSH_MESSAGE, Ttl(DAY_IN_SECONDS))
//
//        subscriptionStorageRepository.getAccountByTopic(pushTopic)?.let { caip10Account ->
//            val account = if (caip10Account.contains(Regex(".:.:."))) {
//                caip10Account.split(":").last()
//            } else {
//                caip10Account
//            }
//
//            castRepository.notify(
//                message.title,
//                message.body,
//                message.icon,
//                message.url,
//                listOf(account),
//                { castNotifyResponse ->
//                    logger.log("$castNotifyResponse")
//                },
//                onFailure
//            )
//        } ?: onFailure(IllegalStateException("No account found for topic: $pushTopic"))
//
//        jsonRpcInteractor.publishJsonRpcRequest(Topic(pushTopic), irnParams, request,
//            onSuccess = {
//                logger.log("Push Message sent successfully")
//            },
//            onFailure = { error ->
//                logger.error("Failed to sent push message: ${error.stackTraceToString()}")
//                onFailure(error)
//            }
//        )
    }

    suspend fun delete(topic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
//        val deleteParams = PushParams.DeleteParams(6000, "User Disconnected")
//        val request = PushRpc.PushDelete(params = deleteParams)
//        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))
//
//        subscriptionStorageRepository.deleteSubscriptionByPushTopic(topic)
//        castRepository.deletePendingRequest(topic)
//
//        jsonRpcInteractor.unsubscribe(Topic(topic))
//        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request,
//            onSuccess = {
//                logger.log("Delete sent successfully")
//            },
//            onFailure = {
//                onFailure(it)
//            }
//        )
    }

    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription> = emptyMap()
//        subscriptionStorageRepository.getAllSubscriptions()
//            .filter { subscription -> !subscription.subscriptionTopic?.value.isNullOrBlank() }
//            .associateBy { subscription -> subscription.subscriptionTopic!!.value }

    private suspend fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PushParams }
            .onEach { request ->
                when (val params = request.params) {
                    is PushParams.DeleteParams -> onPushDelete(request)
                }
            }.launchIn(scope)

    private suspend fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is PushParams }
            .onEach { response ->
                when (val params = response.params) {
                    is PushParams.ProposeParams -> onPushProposeResponse(response, params)
                    is PushParams.MessageParams -> onPushMessageResponse()
                    is PushParams.DeleteParams -> onPushDeleteResponse()
                }
            }.launchIn(scope)

    private suspend fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private suspend fun onPushDelete(request: WCRequest) = supervisorScope {
//        jsonRpcInteractor.unsubscribe(request.topic)
//        subscriptionStorageRepository.deleteSubscriptionByPushTopic(request.topic.value)
//
//        scope.launch { _engineEvent.emit(EngineDO.PushDelete(request.topic.value)) }
    }

    private suspend fun onPushProposeResponse(wcResponse: WCResponse, params: PushParams.ProposeParams) = supervisorScope {
//        try {
//            when (val response = wcResponse.response) {
//                is JsonRpcResponse.JsonRpcResult -> {
//                    val selfPublicKey = PublicKey(params.publicKey)
//                    val pushRequestResponse = response.result as PushParams.ProposeResponseParams
//                    val pushSubscriptionJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(pushRequestResponse.subscriptionAuth).getOrElse { error ->
//                        _engineEvent.emit(SDKError(error))
//                        return@supervisorScope
//                    }
//                    val walletPublicKey = decodeX25519DidKey(pushSubscriptionJwtClaim.issuer)
//                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, walletPublicKey)
//                    val expiry = calcExpiry()
//
//                    val dappScopesFromPushConfigs: Map<String, EngineDO.PushScope.Cached> = extractPushConfigUseCase(params.metaData.url.toUri())
//                        .getOrDefault(emptyList())
//                        .associate { pushScopeRemote ->
//                            pushScopeRemote.name to EngineDO.PushScope.Cached(pushScopeRemote.name, pushScopeRemote.description, true)
//                        }
//                    val respondedPushScope = dappScopesFromPushConfigs.mapValues { (_, pushScopeCached) ->
//                        pushScopeCached.copy(isSelected = pushScopeCached.name in params.scope)
//                    }
//
//                    val respondedSubscription = EngineDO.PushSubscription(
//                        requestId = wcResponse.response.id,
//                        keyAgreementTopic = Topic(""),
//                        responseTopic = wcResponse.topic,
//                        peerPublicKey = PublicKey(walletPublicKey.keyAsHex),
//                        subscriptionTopic = pushTopic,
//                        account = AccountId(params.account),
//                        relay = RelayProtocolOptions(),
//                        metadata = params.metaData,
//                        didJwt = pushRequestResponse.subscriptionAuth,
//                        scope = respondedPushScope,
//                        expiry = expiry
//                    )
//
//                    withContext(Dispatchers.IO) {
//                        with(respondedSubscription) {
//                            subscriptionStorageRepository.insertSubscription(
//                                requestId = requestId,
//                                keyAgreementTopic = keyAgreementTopic.value,
//                                responseTopic = responseTopic.value,
//                                peerPublicKeyAsHex = peerPublicKey?.keyAsHex,
//                                subscriptionTopic = subscriptionTopic?.value,
//                                account = account.value,
//                                relayProtocol = relay.protocol,
//                                relayData = relay.data,
//                                name = metadata.name,
//                                description = metadata.description,
//                                url = metadata.url,
//                                icons = metadata.icons,
//                                native = metadata.redirect?.native,
//                                didJwt = pushRequestResponse.subscriptionAuth,
//                                mapOfScope = scope.mapValues { (_, pushScopeCached) -> pushScopeCached.description to pushScopeCached.isSelected },
//                                expiry = expiry.seconds
//                            )
//                        }
//                    }
//
//                    jsonRpcInteractor.subscribe(pushTopic)
//
//                    val symKey = crypto.getSymmetricKey(pushTopic.value)
//                    val relayUrl = wcKoinApp.koin.get<String>(named(PushDITags.CAST_SERVER_URL))
//                    val account = if (params.account.contains(Regex(".:.:."))) {
//                        params.account.split(":").last()
//                    } else {
//                        params.account
//                    }
//                    castRepository.register(account, symKey.keyAsHex, pushRequestResponse.subscriptionAuth, relayUrl, wcResponse.topic.value) { error ->
//                        _engineEvent.emit(SDKError(error))
//                    }
//
//                    _engineEvent.emit(EngineDO.PushRequestResponse(respondedSubscription))
//                }
//
//                is JsonRpcResponse.JsonRpcError -> {
//                    _engineEvent.emit(EngineDO.PushRequestRejected(wcResponse.response.id, response.error.message))
//                }
//            }
//        } catch (e: Exception) {
//            _engineEvent.emit(SDKError(e))
//        }
    }

    private fun onPushMessageResponse() {
        // TODO: Review if we need this
    }

    private fun onPushDeleteResponse() {
        // TODO: Review if we need this
    }

    private suspend fun resubscribeToSubscriptions() {
//        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
//        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}