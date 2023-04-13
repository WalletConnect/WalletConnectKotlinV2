@file:JvmSynthetic

package com.walletconnect.push.dapp.engine

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeX25519DidKey
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.data.jwt.PushSubscriptionJwtClaim
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.dapp.data.CastRepository
import com.walletconnect.push.dapp.di.PushDITags
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.qualifier.named

internal class PushDappEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val selfAppMetaData: AppMetaData,
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
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        resubscribeToSubscriptions()
                    }
                }

                castRepository.retryRegistration()

                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }

                if (jsonRpcResponsesJob == null) {
                    jsonRpcResponsesJob = collectJsonRpcResponses()
                }

                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    fun request(
        pairingTopic: String,
        account: String,
        onSuccess: (Long) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
        val requestParams = PushParams.RequestParams(selfPublicKey.keyAsHex, selfAppMetaData, account)
        val request = PushRpc.PushRequest(params = requestParams)
        val irnParams = IrnParams(Tags.PUSH_REQUEST, Ttl(DAY_IN_SECONDS), true)
        val responseTopic = sha256(selfPublicKey.keyAsBytes)

        jsonRpcInteractor.subscribe(Topic(responseTopic)) { error -> return@subscribe onFailure(error) }

        jsonRpcInteractor.publishJsonRpcRequest(Topic(pairingTopic), irnParams, request,
            onSuccess = {
                logger.log("Push Request sent successfully")
                onSuccess(request.id)
            },
            onFailure = { error ->
                logger.error("Failed to send a push request: $error")
                onFailure(error)
            }
        )
    }

    fun notify(
        pushTopic: String,
        message: EngineDO.PushMessage,
        onFailure: (Throwable) -> Unit,
    ) {
        val messageParams = PushParams.MessageParams(message.title, message.body, message.icon, message.url)
        val request = PushRpc.PushMessage(params = messageParams)
        val irnParams = IrnParams(Tags.PUSH_MESSAGE, Ttl(DAY_IN_SECONDS))

        scope.launch {
            supervisorScope {
                subscriptionStorageRepository.getAccountByTopic(pushTopic)?.let { caip10Account ->
                    val account = if (caip10Account.contains(Regex(".:.:."))) {
                        caip10Account.split(":").last()
                    } else {
                        caip10Account
                    }

                    castRepository.notify(
                        message.title,
                        message.body,
                        message.icon,
                        message.url,
                        listOf(account),
                        { castNotifyResponse ->
                            logger.log("$castNotifyResponse")
                        },
                        onFailure
                    )
                }
            }
        }

        jsonRpcInteractor.publishJsonRpcRequest(Topic(pushTopic), irnParams, request,
            onSuccess = {
                logger.log("Push Message sent successfully")
            },
            onFailure = { error ->
                logger.error("Failed to sent push message: ${error.stackTraceToString()}")
                onFailure(error)
            }
        )
    }

    fun delete(topic: String, onFailure: (Throwable) -> Unit) {
        val deleteParams = PushParams.DeleteParams(6000, "User Disconnected")
        val request = PushRpc.PushDelete(params = deleteParams)
        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))

        scope.launch {
            supervisorScope {
                subscriptionStorageRepository.deleteSubscription(topic)
            }
            supervisorScope {
                castRepository.deletePendingRequest(topic)
            }
        }

        jsonRpcInteractor.unsubscribe(Topic(topic))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request,
            onSuccess = {
                logger.log("Delete sent successfully")
            },
            onFailure = {
                onFailure(it)
            }
        )
    }

    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription> =
        subscriptionStorageRepository.getAllSubscriptions()
            .filter { subscription -> !subscription.topic.isNullOrBlank() }
            .associateBy { subscription -> subscription.topic!! }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PushParams }
            .onEach { request ->
                when (val params = request.params) {
                    is PushParams.DeleteParams -> onPushDelete(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse.onEach { response ->
            when (val params = response.params) {
                is PushParams.RequestParams -> onPushRequestResponse(response, params)
                is PushParams.MessageParams -> onPushMessageResponse()
                is PushParams.DeleteParams -> onPushDeleteResponse()
            }
        }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private suspend fun onPushDelete(request: WCRequest) = supervisorScope {
        jsonRpcInteractor.unsubscribe(request.topic)
        subscriptionStorageRepository.deleteSubscription(request.topic.value)

        scope.launch { _engineEvent.emit(EngineDO.PushDelete(request.topic.value)) }
    }

    private suspend fun onPushRequestResponse(wcResponse: WCResponse, params: PushParams.RequestParams) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val selfPublicKey = PublicKey(params.publicKey)
                    val pushRequestResponse = response.result as PushParams.RequestResponseParams
                    val pushSubscriptionJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(pushRequestResponse.subscriptionAuth).getOrElse { error ->
                        _engineEvent.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val walletPublicKey = decodeX25519DidKey(pushSubscriptionJwtClaim.issuer)
                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, walletPublicKey)
                    val respondedSubscription = EngineDO.PushSubscription(
                        wcResponse.response.id,
                        wcResponse.topic.value,
                        walletPublicKey.keyAsHex,
                        pushTopic.value,
                        AccountId(params.account),
                        RelayProtocolOptions(),
                        params.metaData
                    )

                    withContext(Dispatchers.IO) {
                        with(respondedSubscription) {
                            subscriptionStorageRepository.insertSubscription(
                                requestId,
                                pairingTopic,
                                peerPublicKeyAsHex,
                                topic,
                                account.value,
                                relay.protocol,
                                relay.data,
                                metadata.name,
                                metadata.description,
                                metadata.url,
                                metadata.icons,
                                metadata.redirect?.native
                            )
                        }

                        jsonRpcInteractor.subscribe(pushTopic)

                        val symKey = crypto.getSymmetricKey(pushTopic.value)
                        val relayUrl = wcKoinApp.koin.get<String>(named(PushDITags.CAST_SERVER_URL))
                        val account = if (params.account.contains(Regex(".:.:."))) {
                            params.account.split(":").last()
                        } else {
                            params.account
                        }
                        castRepository.register(account, symKey.keyAsHex, pushRequestResponse.subscriptionAuth, relayUrl, wcResponse.topic.value) { error ->
                            _engineEvent.emit(SDKError(error))
                        }

                        _engineEvent.emit(EngineDO.PushRequestResponse(respondedSubscription))
                    }
                }
                is JsonRpcResponse.JsonRpcError -> {
                    scope.launch { _engineEvent.emit(EngineDO.PushRequestRejected(wcResponse.response.id, response.error.message)) }
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun onPushMessageResponse() {
        // TODO: Review if we need this
    }

    private fun onPushDeleteResponse() {
        // TODO: Review if we need this
    }

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}