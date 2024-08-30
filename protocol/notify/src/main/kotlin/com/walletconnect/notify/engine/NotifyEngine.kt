@file:JvmSynthetic

package com.walletconnect.notify.engine

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.relay.WSSConnectionState
import com.walletconnect.notify.common.JsonRpcMethod
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.notify.engine.calls.GetActiveSubscriptionsUseCaseInterface
import com.walletconnect.notify.engine.calls.GetAllActiveSubscriptionsUseCase
import com.walletconnect.notify.engine.calls.GetNotificationHistoryUseCaseInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesUseCaseInterface
import com.walletconnect.notify.engine.calls.IsRegisteredUseCaseInterface
import com.walletconnect.notify.engine.calls.PrepareRegistrationUseCaseInterface
import com.walletconnect.notify.engine.calls.RegisterUseCaseInterface
import com.walletconnect.notify.engine.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.notify.engine.calls.UnregisterUseCaseInterface
import com.walletconnect.notify.engine.calls.UpdateSubscriptionUseCaseInterface
import com.walletconnect.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import com.walletconnect.notify.engine.requests.OnMessageUseCase
import com.walletconnect.notify.engine.requests.OnSubscriptionsChangedUseCase
import com.walletconnect.notify.engine.responses.OnDeleteResponseUseCase
import com.walletconnect.notify.engine.responses.OnGetNotificationsResponseUseCase
import com.walletconnect.notify.engine.responses.OnSubscribeResponseUseCase
import com.walletconnect.notify.engine.responses.OnUpdateResponseUseCase
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class NotifyEngine(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val pairingHandler: PairingControllerInterface,
    private val subscribeToDappUseCase: SubscribeToDappUseCaseInterface,
    private val updateUseCase: UpdateSubscriptionUseCaseInterface,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCaseInterface,
    private val decryptMessageUseCase: DecryptMessageUseCaseInterface,
    private val unregisterUseCase: UnregisterUseCaseInterface,
    private val getNotificationTypesUseCase: GetNotificationTypesUseCaseInterface,
    private val getActiveSubscriptionsUseCase: GetActiveSubscriptionsUseCaseInterface,
    private val getAllActiveSubscriptionsUseCase: GetAllActiveSubscriptionsUseCase,
    private val getNotificationHistoryUseCase: GetNotificationHistoryUseCaseInterface,
    private val onMessageUseCase: OnMessageUseCase,
    private val onSubscriptionsChangedUseCase: OnSubscriptionsChangedUseCase,
    private val onSubscribeResponseUseCase: OnSubscribeResponseUseCase,
    private val onUpdateResponseUseCase: OnUpdateResponseUseCase,
    private val onDeleteResponseUseCase: OnDeleteResponseUseCase,
    private val onWatchSubscriptionsResponseUseCase: OnWatchSubscriptionsResponseUseCase,
    private val onGetNotificationsResponseUseCase: OnGetNotificationsResponseUseCase,
    private val watchSubscriptionsForEveryRegisteredAccountUseCase: WatchSubscriptionsForEveryRegisteredAccountUseCase,
    private val isRegisteredUseCase: IsRegisteredUseCaseInterface,
    private val prepareRegistrationUseCase: PrepareRegistrationUseCaseInterface,
    private val registerUseCase: RegisterUseCaseInterface,
) : SubscribeToDappUseCaseInterface by subscribeToDappUseCase,
    UpdateSubscriptionUseCaseInterface by updateUseCase,
    DeleteSubscriptionUseCaseInterface by deleteSubscriptionUseCase,
    DecryptMessageUseCaseInterface by decryptMessageUseCase,
    RegisterUseCaseInterface by registerUseCase,
    UnregisterUseCaseInterface by unregisterUseCase,
    GetNotificationTypesUseCaseInterface by getNotificationTypesUseCase,
    GetActiveSubscriptionsUseCaseInterface by getActiveSubscriptionsUseCase,
    GetNotificationHistoryUseCaseInterface by getNotificationHistoryUseCase,
    IsRegisteredUseCaseInterface by isRegisteredUseCase,
    PrepareRegistrationUseCaseInterface by prepareRegistrationUseCase {
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
            JsonRpcMethod.WC_NOTIFY_GET_NOTIFICATIONS,
        )
    }

    suspend fun setup() {
        jsonRpcInteractor.onResubscribe
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
            }.launchIn(scope)
    }

	private suspend fun handleWSSState(state: WSSConnectionState) {
		when (state) {
			is WSSConnectionState.Disconnected.ConnectionFailed ->
				_engineEvent.emit(ConnectionState(false, ConnectionState.Reason.ConnectionFailed(state.throwable)))

			is WSSConnectionState.Disconnected.ConnectionClosed ->
				_engineEvent.emit(ConnectionState(false, ConnectionState.Reason.ConnectionClosed(state.message ?: "Connection closed")))

			else -> _engineEvent.emit(ConnectionState(true))
		}
	}

    private suspend fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is CoreNotifyParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is CoreNotifyParams.MessageParams -> onMessageUseCase(request, requestParams)
                    is CoreNotifyParams.SubscriptionsChangedParams -> onSubscriptionsChangedUseCase(request, requestParams)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is CoreNotifyParams }
            .onEach { response ->
                when (val params = response.params) {
                    is CoreNotifyParams.SubscribeParams -> onSubscribeResponseUseCase(response, params)
                    is CoreNotifyParams.UpdateParams -> onUpdateResponseUseCase(response, params)
                    is CoreNotifyParams.WatchSubscriptionsParams -> onWatchSubscriptionsResponseUseCase(response)
                    is CoreNotifyParams.DeleteParams -> onDeleteResponseUseCase(response, params)
                    is CoreNotifyParams.GetNotificationsParams -> onGetNotificationsResponseUseCase(response, params)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job = merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
        .onEach { exception -> _engineEvent.emit(exception) }
        .launchIn(scope)

    private fun collectNotifyEvents(): Job = merge(onMessageUseCase.events, onWatchSubscriptionsResponseUseCase.events, onSubscriptionsChangedUseCase.events)
        .onEach { event -> _engineEvent.emit(event) }
        .launchIn(scope)


    private suspend fun watchSubscriptionsForEveryRegisteredAccount() {
        watchSubscriptionsForEveryRegisteredAccountUseCase()
    }

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getAllActiveSubscriptionsUseCase().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}


//todo: Extract to Validator class and add more tests
internal val blockingCallsMinTimeout = 5.seconds
internal val blockingCallsMaxTimeout = 60.seconds
internal val blockingCallsDefaultTimeout = blockingCallsMaxTimeout
internal val blockingCallsDelayInterval = 10.milliseconds

internal fun Duration?.validateTimeout() =
    if (this == null) blockingCallsDefaultTimeout
    else if (this < blockingCallsMinTimeout)
        throw Exception("Timeout has to be at least $blockingCallsMinTimeout")
    else if (this > blockingCallsMaxTimeout)
        throw Exception("Timeout has to be at most $blockingCallsMaxTimeout")
    else this