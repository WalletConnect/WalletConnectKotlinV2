package com.walletconnect.sync.engine.domain

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.sync.common.json_rpc.JsonRpcMethod
import com.walletconnect.sync.common.json_rpc.SyncParams
import com.walletconnect.sync.engine.use_case.calls.*
import com.walletconnect.sync.engine.use_case.subscriptions.SubscribeToAllStoresUpdatesUseCase
import com.walletconnect.sync.engine.use_case.requests.incoming.OnDeleteRequestUseCase
import com.walletconnect.sync.engine.use_case.requests.incoming.OnSetRequestUseCase
import com.walletconnect.sync.engine.use_case.responses.OnDeleteResponseUseCase
import com.walletconnect.sync.engine.use_case.responses.OnSetResponseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class SyncEngine(
    private val getStoresUseCase: GetStoresUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val createStoreUseCase: CreateStoreUseCase,
    private val deleteStoreValueUseCase: DeleteStoreValueUseCase,
    private val setStoreValueUseCase: SetStoreValueUseCase,
    private val pairingHandler: PairingControllerInterface,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val onSetRequestUseCase: OnSetRequestUseCase,
    private val onDeleteRequestUseCase: OnDeleteRequestUseCase,
    private val onSetResponseUseCase: OnSetResponseUseCase,
    private val onDeleteResponseUseCase: OnDeleteResponseUseCase,
    private val subscribeToAllStoresUpdatesUseCase: SubscribeToAllStoresUpdatesUseCase,
) : GetMessageUseCaseInterface by GetMessageUseCase,
    CreateUseCaseInterface by createStoreUseCase,
    GetStoresUseCaseInterface by getStoresUseCase,
    RegisterUseCaseInterface by registerAccountUseCase,
    DeleteUseCaseInterface by deleteStoreValueUseCase,
    SetUseCaseInterface by setStoreValueUseCase {

    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_SYNC_SET,
            JsonRpcMethod.WC_SYNC_DELETE,
        )
    }

    fun setup() {
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
                if (jsonRpcResponsesJob == null) {
                    jsonRpcResponsesJob = collectPeerResponses()
                }
                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
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

    private fun collectPeerResponses(): Job =
        scope.launch {
            jsonRpcInteractor.peerResponse.collect { response ->
                when (val params = response.params) {
                    is SyncParams.SetParams -> onSetResponseUseCase(params, response)
                    is SyncParams.DeleteParams -> onDeleteResponseUseCase(params, response)
                }
            }
        }

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _events.emit(exception) }
            .launchIn(scope)
}