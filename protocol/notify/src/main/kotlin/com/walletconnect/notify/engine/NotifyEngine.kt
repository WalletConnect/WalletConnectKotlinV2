@file:JvmSynthetic

package com.walletconnect.notify.engine

import com.walletconnect.android.echo.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.notify.common.JsonRpcMethod
import com.walletconnect.notify.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfMessagesUseCaseInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesUseCaseInterface
import com.walletconnect.notify.engine.calls.RegisterUseCaseInterface
import com.walletconnect.notify.engine.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.notify.engine.calls.UnregisterUseCaseInterface
import com.walletconnect.notify.engine.calls.UpdateSubscriptionRequestUseCaseInterface
import com.walletconnect.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import com.walletconnect.notify.engine.requests.OnNotifyDeleteUseCase
import com.walletconnect.notify.engine.requests.OnNotifyMessageUseCase
import com.walletconnect.notify.engine.requests.OnSubscriptionsChangedUseCase
import com.walletconnect.notify.engine.responses.OnNotifySubscribeResponseUseCase
import com.walletconnect.notify.engine.responses.OnNotifyUpdateResponseUseCase
import com.walletconnect.notify.engine.responses.OnWatchSubscriptionsResponseUseCase
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

internal class NotifyEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingHandler: PairingControllerInterface,
    private val subscribeToDappUseCase: SubscribeToDappUseCaseInterface,
    private val updateUseCase: UpdateSubscriptionRequestUseCaseInterface,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCaseInterface,
    private val deleteMessageUseCase: DeleteMessageUseCaseInterface,
    private val decryptMessageUseCase: DecryptMessageUseCaseInterface,
    private val registerUseCase: RegisterUseCaseInterface,
    private val unregisterUseCase: UnregisterUseCaseInterface,
    private val getNotificationTypesUseCase: GetNotificationTypesUseCaseInterface,
    private val getListOfActiveSubscriptionsUseCase: GetListOfActiveSubscriptionsUseCaseInterface,
    private val getListOfMessages: GetListOfMessagesUseCaseInterface,
    private val onNotifyMessageUseCase: OnNotifyMessageUseCase,
    private val onNotifyDeleteUseCase: OnNotifyDeleteUseCase,
    private val onSubscriptionsChangedUseCase: OnSubscriptionsChangedUseCase,
    private val onNotifySubscribeResponseUseCase: OnNotifySubscribeResponseUseCase,
    private val onNotifyUpdateResponseUseCase: OnNotifyUpdateResponseUseCase,
    private val onWatchSubscriptionsResponseUseCase: OnWatchSubscriptionsResponseUseCase,
    private val watchSubscriptionsForEveryRegisteredAccountUseCase: WatchSubscriptionsForEveryRegisteredAccountUseCase,
) : SubscribeToDappUseCaseInterface by subscribeToDappUseCase,
    UpdateSubscriptionRequestUseCaseInterface by updateUseCase,
    DeleteSubscriptionUseCaseInterface by deleteSubscriptionUseCase,
    DeleteMessageUseCaseInterface by deleteMessageUseCase,
    DecryptMessageUseCaseInterface by decryptMessageUseCase,
    RegisterUseCaseInterface by registerUseCase,
    UnregisterUseCaseInterface by unregisterUseCase,
    GetNotificationTypesUseCaseInterface by getNotificationTypesUseCase,
    GetListOfActiveSubscriptionsUseCaseInterface by getListOfActiveSubscriptionsUseCase,
    GetListOfMessagesUseCaseInterface by getListOfMessages {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var notifyEventsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_NOTIFY_SUBSCRIBE,
            JsonRpcMethod.WC_NOTIFY_MESSAGE,
            JsonRpcMethod.WC_NOTIFY_UPDATE,
            JsonRpcMethod.WC_NOTIFY_DELETE,
            JsonRpcMethod.WC_NOTIFY_WATCH_SUBSCRIPTIONS,
            JsonRpcMethod.WC_NOTIFY_SUBSCRIPTIONS_CHANGED,
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
                        watchSubscriptionsForEveryRegisteredAccount()
                    }
                }

                if (jsonRpcRequestsJob == null) jsonRpcRequestsJob = collectJsonRpcRequests()
                if (jsonRpcResponsesJob == null) jsonRpcResponsesJob = collectJsonRpcResponses()
                if (internalErrorsJob == null) internalErrorsJob = collectInternalErrors()
                if (notifyEventsJob == null) notifyEventsJob = collectNotifyEvents()
            }
            .launchIn(scope)
    }

    private suspend fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is CoreNotifyParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is CoreNotifyParams.MessageParams -> onNotifyMessageUseCase(request, requestParams)
                    is CoreNotifyParams.DeleteParams -> onNotifyDeleteUseCase(request, requestParams)
                    is CoreNotifyParams.SubscriptionsChangedParams -> onSubscriptionsChangedUseCase(request, requestParams)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is CoreNotifyParams }
            .onEach { response ->
                when (val params = response.params) {
                    is CoreNotifyParams.SubscribeParams -> onNotifySubscribeResponseUseCase(response, params)
                    is CoreNotifyParams.UpdateParams -> onNotifyUpdateResponseUseCase(response, params)
                    is CoreNotifyParams.WatchSubscriptionsParams -> onWatchSubscriptionsResponseUseCase(response, params)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectNotifyEvents(): Job = merge(
        onNotifySubscribeResponseUseCase.events, onNotifyMessageUseCase.events, onNotifyUpdateResponseUseCase.events, onNotifyDeleteUseCase.events,
        onWatchSubscriptionsResponseUseCase.events, onSubscriptionsChangedUseCase.events
    )
        .onEach { event -> _engineEvent.emit(event) }
        .launchIn(scope)


    private suspend fun watchSubscriptionsForEveryRegisteredAccount() {
        watchSubscriptionsForEveryRegisteredAccountUseCase()
    }

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}