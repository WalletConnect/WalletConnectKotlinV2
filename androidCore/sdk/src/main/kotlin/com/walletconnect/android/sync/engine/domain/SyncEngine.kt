package com.walletconnect.android.sync.engine.domain

import com.walletconnect.android.history.HistoryInterface
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.sync.common.json_rpc.JsonRpcMethod
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.android.sync.engine.use_case.calls.CreateStoreUseCase
import com.walletconnect.android.sync.engine.use_case.calls.CreateUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.DeleteStoreValueUseCase
import com.walletconnect.android.sync.engine.use_case.calls.DeleteUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.GetMessageUseCase
import com.walletconnect.android.sync.engine.use_case.calls.GetMessageUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.GetStoreTopicUseCase
import com.walletconnect.android.sync.engine.use_case.calls.GetStoreTopicUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.GetStoresUseCase
import com.walletconnect.android.sync.engine.use_case.calls.GetStoresUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.IsAccountRegisteredUseCase
import com.walletconnect.android.sync.engine.use_case.calls.IsAccountRegisteredUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.RegisterAccountUseCase
import com.walletconnect.android.sync.engine.use_case.calls.RegisterAccountUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.calls.SetStoreValueUseCase
import com.walletconnect.android.sync.engine.use_case.calls.SetUseCaseInterface
import com.walletconnect.android.sync.engine.use_case.requests.OnDeleteRequestUseCase
import com.walletconnect.android.sync.engine.use_case.requests.OnSetRequestUseCase
import com.walletconnect.android.sync.engine.use_case.subscriptions.SubscribeToAllStoresUpdatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class SyncEngine(
    private val getStoresUseCase: GetStoresUseCase,
    private val getStoreTopicUseCase: GetStoreTopicUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val isAccountRegisteredUseCase: IsAccountRegisteredUseCase,
    private val createStoreUseCase: CreateStoreUseCase,
    private val deleteStoreValueUseCase: DeleteStoreValueUseCase,
    private val setStoreValueUseCase: SetStoreValueUseCase,
    private val pairingHandler: PairingControllerInterface,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val onSetRequestUseCase: OnSetRequestUseCase,
    private val onDeleteRequestUseCase: OnDeleteRequestUseCase,
    private val subscribeToAllStoresUpdatesUseCase: SubscribeToAllStoresUpdatesUseCase,
    private val historyInterface: HistoryInterface,
) : GetMessageUseCaseInterface by GetMessageUseCase,
    CreateUseCaseInterface by createStoreUseCase,
    GetStoresUseCaseInterface by getStoresUseCase,
    GetStoreTopicUseCaseInterface by getStoreTopicUseCase,
    RegisterAccountUseCaseInterface by registerAccountUseCase,
    IsAccountRegisteredUseCaseInterface by isAccountRegisteredUseCase,
    DeleteUseCaseInterface by deleteStoreValueUseCase,
    SetUseCaseInterface by setStoreValueUseCase {

    private var jsonRpcRequestsJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var internalUseCaseJob: Job? = null

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_SYNC_SET,
            JsonRpcMethod.WC_SYNC_DELETE,
        )
    }

    fun setup() {
//        scope.launch { registerTagsInHistory() }
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _events.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) {
                        subscribeToAllStoresUpdatesUseCase(onError = { error -> scope.launch { _events.emit(SDKError(error)) } })
                    }
                }
                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }
                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
                if (internalUseCaseJob == null) {
                    internalUseCaseJob = collectUseCaseEvents()
                }
            }
            .launchIn(scope)

    }


    private suspend fun registerTagsInHistory() {
        // Has to be one register call per clientId
        // Currently moved to PushWalletEgnine
//        historyInterface.registerTags(tags = listOf(Tags.SYNC_SET, Tags.SYNC_DELETE), {}, {})
    }


    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is SyncParams }
            .onEach { request ->
                when (val params = request.params) {
                    is SyncParams.SetParams -> onSetRequestUseCase(params, request)
                    is SyncParams.DeleteParams -> onDeleteRequestUseCase(params, request)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _events.emit(exception) }
            .launchIn(scope)

    private fun collectUseCaseEvents(): Job =
        merge(onSetRequestUseCase.events, onDeleteRequestUseCase.events)
            .onEach { event -> _events.emit(event) }
            .launchIn(scope)
}