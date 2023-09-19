@file:JvmSynthetic

package com.walletconnect.push.engine

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.engine.calls.ApproveSubscriptionRequestUseCaseInterface
import com.walletconnect.push.engine.calls.DecryptMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.push.engine.calls.EnableSyncUseCaseInterface
import com.walletconnect.push.engine.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.push.engine.calls.GetListOfMessagesUseCaseInterface
import com.walletconnect.push.engine.calls.RejectSubscriptionRequestUseCaseInterface
import com.walletconnect.push.engine.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.push.engine.calls.UpdateSubscriptionRequestUseCaseInterface
import com.walletconnect.push.engine.requests.OnPushDeleteUseCase
import com.walletconnect.push.engine.requests.OnPushMessageUseCase
import com.walletconnect.push.engine.requests.OnPushProposeUseCase
import com.walletconnect.push.engine.responses.OnPushSubscribeResponseUseCase
import com.walletconnect.push.engine.responses.OnPushUpdateResponseUseCase
import com.walletconnect.push.engine.sync.use_case.events.OnSyncUpdateEventUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class PushWalletEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingHandler: PairingControllerInterface,
    private val syncClient: SyncInterface,
    private val onSyncUpdateEventUseCase: OnSyncUpdateEventUseCase,
    private val subscribeUserCase: SubscribeToDappUseCaseInterface,
    private val approveUseCase: ApproveSubscriptionRequestUseCaseInterface,
    private val rejectUserCase: RejectSubscriptionRequestUseCaseInterface,
    private val updateUseCase: UpdateSubscriptionRequestUseCaseInterface,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCaseInterface,
    private val deleteMessageUseCase: DeleteMessageUseCaseInterface,
    private val decryptMessageUseCase: DecryptMessageUseCaseInterface,
    private val enableSyncUseCase: EnableSyncUseCaseInterface,
    private val getListOfActiveSubscriptionsUseCase: GetListOfActiveSubscriptionsUseCaseInterface,
    private val getListOfMessages: GetListOfMessagesUseCaseInterface,
    private val onPushProposeUseCase: OnPushProposeUseCase,
    private val onPushMessageUseCase: OnPushMessageUseCase,
    private val onPushDeleteUseCase: OnPushDeleteUseCase,
    private val onPushSubscribeResponseUseCase: OnPushSubscribeResponseUseCase,
    private val onPushUpdateResponseUseCase: OnPushUpdateResponseUseCase,
    private val logger: Logger,
) : SubscribeToDappUseCaseInterface by subscribeUserCase,
    ApproveSubscriptionRequestUseCaseInterface by approveUseCase,
    RejectSubscriptionRequestUseCaseInterface by rejectUserCase,
    UpdateSubscriptionRequestUseCaseInterface by updateUseCase,
    DeleteSubscriptionUseCaseInterface by deleteSubscriptionUseCase,
    DeleteMessageUseCaseInterface by deleteMessageUseCase,
    DecryptMessageUseCaseInterface by decryptMessageUseCase,
    EnableSyncUseCaseInterface by enableSyncUseCase,
    GetListOfActiveSubscriptionsUseCaseInterface by getListOfActiveSubscriptionsUseCase,
    GetListOfMessagesUseCaseInterface by getListOfMessages {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var syncUpdateEventsJob: Job? = null
    private var pushEventsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_PUSH_REQUEST,
            JsonRpcMethod.WC_PUSH_MESSAGE,
            JsonRpcMethod.WC_PUSH_DELETE,
            JsonRpcMethod.WC_PUSH_PROPOSE,
            JsonRpcMethod.WC_PUSH_SUBSCRIBE,
            JsonRpcMethod.WC_PUSH_UPDATE,
        )
    }

    suspend fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        resubscribeToSubscriptions()
                    }
                }

                if (jsonRpcRequestsJob == null) jsonRpcRequestsJob = collectJsonRpcRequests()
                if (jsonRpcResponsesJob == null) jsonRpcResponsesJob = collectJsonRpcResponses()
                if (internalErrorsJob == null) internalErrorsJob = collectInternalErrors()
                if (syncUpdateEventsJob == null) syncUpdateEventsJob = collectSyncUpdateEvents()
                if (pushEventsJob == null) pushEventsJob = collectPushEvents()
            }
            .launchIn(scope)
    }

    private suspend fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PushParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PushParams.ProposeParams -> onPushProposeUseCase(request, requestParams)
                    is PushParams.MessageParams -> onPushMessageUseCase(request, requestParams)
                    is PushParams.DeleteParams -> onPushDeleteUseCase(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is PushParams }
            .onEach { response ->
                when (val responseParams = response.params) {
                    is PushParams.SubscribeParams -> onPushSubscribeResponseUseCase(response)
                    is PushParams.UpdateParams -> onPushUpdateResponseUseCase(response, responseParams)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectSyncUpdateEvents(): Job = syncClient.onSyncUpdateEvents
        .onEach { event -> onSyncUpdateEventUseCase(event) }
        .launchIn(scope)

    private fun collectPushEvents(): Job =
        merge(onPushProposeUseCase.events, onPushMessageUseCase.events, onPushDeleteUseCase.events, onPushSubscribeResponseUseCase.events, onPushUpdateResponseUseCase.events)
            .onEach { event -> _engineEvent.emit(event) }
            .launchIn(scope)

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}