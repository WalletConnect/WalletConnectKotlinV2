@file:JvmSynthetic

package com.walletconnect.notify.engine

import com.walletconnect.android.history.HistoryInterface
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.JsonRpcMethod
import com.walletconnect.notify.engine.calls.DecryptMessageUseCaseInterface
import com.walletconnect.notify.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.notify.engine.calls.EnableSyncUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfMessagesUseCaseInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesUseCase
import com.walletconnect.notify.engine.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.notify.engine.calls.UpdateSubscriptionRequestUseCaseInterface
import com.walletconnect.notify.engine.requests.OnNotifyDeleteUseCase
import com.walletconnect.notify.engine.requests.OnNotifyMessageUseCase
import com.walletconnect.notify.engine.responses.OnNotifySubscribeResponseUseCase
import com.walletconnect.notify.engine.responses.OnNotifyUpdateResponseUseCase
import com.walletconnect.notify.engine.sync.use_case.events.OnSyncUpdateEventUseCase
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
    private val syncClient: SyncInterface,
    private val onSyncUpdateEventUseCase: OnSyncUpdateEventUseCase,
    private val historyInterface: HistoryInterface,
    private val subscribeToDappUseCase: SubscribeToDappUseCaseInterface,
    private val updateUseCase: UpdateSubscriptionRequestUseCaseInterface,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCaseInterface,
    private val deleteMessageUseCase: DeleteMessageUseCaseInterface,
    private val decryptMessageUseCase: DecryptMessageUseCaseInterface,
    private val enableSyncUseCase: EnableSyncUseCaseInterface,
    private val getNotificationTypesUseCase: GetNotificationTypesUseCase,
    private val getListOfActiveSubscriptionsUseCase: GetListOfActiveSubscriptionsUseCaseInterface,
    private val getListOfMessages: GetListOfMessagesUseCaseInterface,
    private val onNotifyMessageUseCase: OnNotifyMessageUseCase,
    private val onNotifyDeleteUseCase: OnNotifyDeleteUseCase,
    private val onNotifySubscribeResponseUseCase: OnNotifySubscribeResponseUseCase,
    private val onNotifyUpdateResponseUseCase: OnNotifyUpdateResponseUseCase,
    private val logger: Logger,
) : SubscribeToDappUseCaseInterface by subscribeToDappUseCase,
    UpdateSubscriptionRequestUseCaseInterface by updateUseCase,
    DeleteSubscriptionUseCaseInterface by deleteSubscriptionUseCase,
    DeleteMessageUseCaseInterface by deleteMessageUseCase,
    DecryptMessageUseCaseInterface by decryptMessageUseCase,
    EnableSyncUseCaseInterface by enableSyncUseCase,
    GetNotificationTypesInterface by getNotificationTypesUseCase,
    GetListOfActiveSubscriptionsUseCaseInterface by getListOfActiveSubscriptionsUseCase,
    GetListOfMessagesUseCaseInterface by getListOfMessages {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var syncUpdateEventsJob: Job? = null
    private var notifyEventsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_NOTIFY_MESSAGE,
            JsonRpcMethod.WC_NOTIFY_DELETE,
            JsonRpcMethod.WC_NOTIFY_SUBSCRIBE,
            JsonRpcMethod.WC_NOTIFY_UPDATE,
        )
    }

    suspend fun setup() {
        registerTagsInHistory()
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
                if (notifyEventsJob == null) notifyEventsJob = collectNotifyEvents()
            }
            .launchIn(scope)
    }

    private suspend fun registerTagsInHistory() {
        // Sync are here since History Server expects only one register call
        historyInterface.registerTags(tags = listOf(Tags.NOTIFY_MESSAGE, Tags.SYNC_SET, Tags.SYNC_DELETE), {}, { error -> logger.error(error.throwable) })
    }

    private suspend fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is NotifyParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is NotifyParams.MessageParams -> onNotifyMessageUseCase(request, requestParams)
                    is NotifyParams.DeleteParams -> onNotifyDeleteUseCase(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is NotifyParams }
            .onEach { response ->
                when (val responseParams = response.params) {
                    is NotifyParams.SubscribeParams -> onNotifySubscribeResponseUseCase(response)
                    is NotifyParams.UpdateParams -> onNotifyUpdateResponseUseCase(response, responseParams)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectSyncUpdateEvents(): Job = syncClient.onSyncUpdateEvents
        .onEach { event -> onSyncUpdateEventUseCase(event) }
        .launchIn(scope)

    private fun collectNotifyEvents(): Job = merge(onNotifySubscribeResponseUseCase.events, onNotifyMessageUseCase.events, onNotifyUpdateResponseUseCase.events, onNotifyDeleteUseCase.events)
        .onEach { event -> _engineEvent.emit(event) }
        .launchIn(scope)

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}