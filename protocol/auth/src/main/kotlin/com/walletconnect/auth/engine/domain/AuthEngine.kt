@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.model.Events
import com.walletconnect.auth.engine.pairingTopicToResponseTopicMap
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCaseInterface
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByTopicUseCase
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.use_case.calls.FormatMessageUseCaseInterface
import com.walletconnect.auth.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.auth.use_case.calls.GetVerifyContextUseCaseInterface
import com.walletconnect.auth.use_case.calls.RespondAuthRequestUseCaseInterface
import com.walletconnect.auth.use_case.calls.SendAuthRequestUseCaseInterface
import com.walletconnect.auth.use_case.requests.OnAuthRequestUseCase
import com.walletconnect.auth.use_case.responses.OnAuthRequestResponseUseCase
import com.walletconnect.utils.Empty
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

internal class AuthEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val getPendingJsonRpcHistoryEntriesUseCase: GetPendingJsonRpcHistoryEntriesUseCaseInterface,
    private val getPendingJsonRpcHistoryEntryByTopicUseCase: GetPendingJsonRpcHistoryEntryByTopicUseCase,
    private val pairingHandler: PairingControllerInterface,
    private val sendAuthRequestUseCase: SendAuthRequestUseCaseInterface,
    private val respondAuthRequestUseCase: RespondAuthRequestUseCaseInterface,
    private val formatMessageUseCase: FormatMessageUseCaseInterface,
    private val getVerifyContextUseCase: GetVerifyContextUseCaseInterface,
    private val getListOfVerifyContextsUseCase: GetListOfVerifyContextsUseCaseInterface,
    private val onAuthRequestUseCase: OnAuthRequestUseCase,
    private val onAuthRequestResponseUseCase: OnAuthRequestResponseUseCase,
) : SendAuthRequestUseCaseInterface by sendAuthRequestUseCase,
    RespondAuthRequestUseCaseInterface by respondAuthRequestUseCase,
    FormatMessageUseCaseInterface by formatMessageUseCase,
    GetPendingJsonRpcHistoryEntriesUseCaseInterface by getPendingJsonRpcHistoryEntriesUseCase,
    GetVerifyContextUseCaseInterface by getVerifyContextUseCase,
    GetListOfVerifyContextsUseCaseInterface by getListOfVerifyContextsUseCase {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var authEventsJob: Job? = null

    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(JsonRpcMethod.WC_AUTH_REQUEST)

        pairingHandler.activePairingFlow
            .onEach { pairingTopic ->
                try {
                    val request = getPendingJsonRpcHistoryEntryByTopicUseCase(pairingTopic)
                    val context = verifyContextStorageRepository.get(request.id) ?: VerifyContext(request.id, String.Empty, Validation.UNKNOWN, String.Empty)
                    scope.launch { _engineEvent.emit(Events.OnAuthRequest(request.id,request.pairingTopic, request.payloadParams, context)) }
                } catch (e: Exception) {
                    println("No auth request for pairing topic")
                }
            }.launchIn(scope)
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) { resubscribeToPendingRequestsTopics() }
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
                if (authEventsJob == null) {
                    authEventsJob = collectAuthEvents()
                }
            }
            .launchIn(scope)
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is AuthParams.RequestParams }
            .onEach { request -> onAuthRequestUseCase(request, request.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is AuthParams }
            .onEach { response -> onAuthRequestResponseUseCase(response, response.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun resubscribeToPendingRequestsTopics() {
        val responseTopics = pairingTopicToResponseTopicMap.map { (_, responseTopic) -> responseTopic.value }
        try {
            jsonRpcInteractor.batchSubscribe(responseTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun collectAuthEvents(): Job =
        merge(sendAuthRequestUseCase.events, onAuthRequestUseCase.events, onAuthRequestResponseUseCase.events)
            .onEach { event -> _engineEvent.emit(event) }
            .launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)
}