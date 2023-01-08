@file:JvmSynthetic

package com.walletconnect.push.dapp.engine

import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.impl.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.storage.data.SubscriptionStorageRepository
import com.walletconnect.util.generateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class PushDappEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val selfAppMetaData: AppMetaData,
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
        val selfPublicKey = crypto.generateKeyPair()
        val requestParams = PushParams.RequestParams(selfPublicKey.keyAsHex, selfAppMetaData, account)
        val request = PushRpc.PushRequest(id = generateId(), params = requestParams)
        val irnParams = IrnParams(Tags.PUSH_REQUEST, Ttl(DAY_IN_SECONDS), true)
        jsonRpcInteractor.subscribe(Topic(pairingTopic)) { error -> return@subscribe onFailure(error) }

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
        val request = PushRpc.PushMessage(id = generateId(), params = messageParams)
        val irnParams = IrnParams(Tags.PUSH_MESSAGE, Ttl(DAY_IN_SECONDS))

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
        val request = PushRpc.PushDelete(id = generateId(), params = deleteParams)
        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))

        subscriptionStorageRepository.delete(topic)

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

    fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription.Responded> {
        return subscriptionStorageRepository.getAllSubscriptions()
            .filterIsInstance<EngineDO.PushSubscription.Responded>()
            .associateBy { subscription -> subscription.topic }
    }

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
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)

    private fun onPushDelete(request: WCRequest) {
        jsonRpcInteractor.unsubscribe(request.topic)
        subscriptionStorageRepository.delete(request.topic.value)

        scope.launch { _engineEvent.emit(EngineDO.PushDelete(request.topic.value)) }
    }

    private fun onPushRequestResponse(wcResponse: WCResponse, params: PushParams.RequestParams) {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val selfPublicKey = PublicKey(params.publicKey)
                    val pushRequestResponse = response.result as PushParams.RequestResponseParams
                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(pushRequestResponse.publicKey))

                    val respondedSubscription = EngineDO.PushSubscription.Responded(wcResponse.response.id, pushRequestResponse.publicKey, pushTopic.value, RelayProtocolOptions(), params.metaData)

                    subscriptionStorageRepository.insertRespondedSubscription(respondedSubscription)
                    jsonRpcInteractor.subscribe(pushTopic)

                    scope.launch { _engineEvent.emit(EngineDO.PushRequestResponse(respondedSubscription)) }
                }
                is JsonRpcResponse.JsonRpcError -> {
                    scope.launch { _engineEvent.emit(EngineDO.PushRequestRejected(wcResponse.response.id, response.error.message)) }
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(InternalError(e))) }
        }
    }

    private fun onPushMessageResponse() {
        // TODO: Review if we need this
    }

    private fun onPushDeleteResponse() {
        // TODO: Review if we need this
    }

    private fun resubscribeToSubscriptions() {
        subscriptionStorageRepository.getAllSubscriptions()
            .filterIsInstance<EngineDO.PushSubscription.Responded>()
            .forEach { subscription ->
                jsonRpcInteractor.subscribe(Topic(subscription.topic))
            }
    }
}