@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.android.history.HistoryInterface
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.engine.sync.use_case.events.OnSyncUpdateEventUseCase
import com.walletconnect.chat.engine.use_case.SubscribeToChatTopicsUseCase
import com.walletconnect.chat.engine.use_case.calls.AcceptInviteUseCase
import com.walletconnect.chat.engine.use_case.calls.AcceptInviteUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.GetMessagesUseCase
import com.walletconnect.chat.engine.use_case.calls.GetMessagesUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.GetReceivedInvitesUseCase
import com.walletconnect.chat.engine.use_case.calls.GetReceivedInvitesUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.GetSentInvitesUseCase
import com.walletconnect.chat.engine.use_case.calls.GetSentInvitesUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.GetThreadsUseCase
import com.walletconnect.chat.engine.use_case.calls.GetThreadsUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.GoPrivateUseCase
import com.walletconnect.chat.engine.use_case.calls.GoPrivateUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.GoPublicUseCase
import com.walletconnect.chat.engine.use_case.calls.GoPublicUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.LeaveThreadUseCase
import com.walletconnect.chat.engine.use_case.calls.LeaveThreadUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.RegisterIdentityUseCase
import com.walletconnect.chat.engine.use_case.calls.RegisterIdentityUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.RejectInviteUseCase
import com.walletconnect.chat.engine.use_case.calls.RejectInviteUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.ResolveAccountUseCase
import com.walletconnect.chat.engine.use_case.calls.ResolveAccountUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.SendInviteUseCase
import com.walletconnect.chat.engine.use_case.calls.SendInviteUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.SendMessageUseCase
import com.walletconnect.chat.engine.use_case.calls.SendMessageUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.SendPingUseCase
import com.walletconnect.chat.engine.use_case.calls.SendPingUseCaseInterface
import com.walletconnect.chat.engine.use_case.calls.UnregisterIdentityUseCase
import com.walletconnect.chat.engine.use_case.calls.UnregisterIdentityUseCaseInterface
import com.walletconnect.chat.engine.use_case.requests.OnInviteRequestUseCase
import com.walletconnect.chat.engine.use_case.requests.OnLeaveRequestUseCase
import com.walletconnect.chat.engine.use_case.requests.OnMessageRequestUseCase
import com.walletconnect.chat.engine.use_case.responses.OnInviteResponseUseCase
import com.walletconnect.chat.engine.use_case.responses.OnLeaveResponseUseCase
import com.walletconnect.chat.engine.use_case.responses.OnMessageResponseUseCase
import com.walletconnect.chat.json_rpc.JsonRpcMethod
import com.walletconnect.foundation.common.model.Ttl
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

internal class ChatEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingHandler: PairingControllerInterface,
    private val subscribeToChatTopicsUseCase: SubscribeToChatTopicsUseCase,
    private val syncClient: SyncInterface,
    private val onSyncUpdateEventUseCase: OnSyncUpdateEventUseCase,
    private val acceptInviteUseCase: AcceptInviteUseCase,
    private val rejectInviteUseCase: RejectInviteUseCase,
    private val goPublicUseCase: GoPublicUseCase,
    private val goPrivateUseCase: GoPrivateUseCase,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val onInviteRequestUseCase: OnInviteRequestUseCase,
    private val onMessageRequestUseCase: OnMessageRequestUseCase,
    private val onLeaveRequestUseCase: OnLeaveRequestUseCase,
    private val onInviteResponseUseCase: OnInviteResponseUseCase,
    private val onMessageResponseUseCase: OnMessageResponseUseCase,
    private val onLeaveResponseUseCase: OnLeaveResponseUseCase,
    private val sendInviteUseCase: SendInviteUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val unregisterIdentityUseCase: UnregisterIdentityUseCase,
    private val resolveAccountUseCase: ResolveAccountUseCase,
    private val leaveThreadUseCase: LeaveThreadUseCase,
    private val sendPingUseCase: SendPingUseCase,
    private val getThreadsUseCase: GetThreadsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getSentInvitesUseCase: GetSentInvitesUseCase,
    private val getReceivedInvitesUseCase: GetReceivedInvitesUseCase,
    private val historyInterface: HistoryInterface,
) : AcceptInviteUseCaseInterface by acceptInviteUseCase,
    RejectInviteUseCaseInterface by rejectInviteUseCase,
    RegisterIdentityUseCaseInterface by registerIdentityUseCase,
    GoPublicUseCaseInterface by goPublicUseCase,
    GoPrivateUseCaseInterface by goPrivateUseCase,
    GetThreadsUseCaseInterface by getThreadsUseCase,
    GetMessagesUseCaseInterface by getMessagesUseCase,
    GetSentInvitesUseCaseInterface by getSentInvitesUseCase,
    GetReceivedInvitesUseCaseInterface by getReceivedInvitesUseCase,
    UnregisterIdentityUseCaseInterface by unregisterIdentityUseCase,
    ResolveAccountUseCaseInterface by resolveAccountUseCase,
    LeaveThreadUseCaseInterface by leaveThreadUseCase,
    SendPingUseCaseInterface by sendPingUseCase,
    SendMessageUseCaseInterface by sendMessageUseCase,
    SendInviteUseCaseInterface by sendInviteUseCase {

    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var syncUpdateEventsJob: Job? = null
    private var chatEventsJob: Job? = null

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    // Remove after pairing refactor
    init {
        pairingHandler.register(
            JsonRpcMethod.WC_CHAT_INVITE,
            JsonRpcMethod.WC_CHAT_MESSAGE,
            JsonRpcMethod.WC_CHAT_LEAVE,
            JsonRpcMethod.WC_CHAT_PING
        )
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _events.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope { launch(Dispatchers.IO) { subscribeToChatTopicsUseCase() } }

                if (jsonRpcRequestsJob == null) jsonRpcRequestsJob = collectJsonRpcRequests()
                if (jsonRpcResponsesJob == null) jsonRpcResponsesJob = collectPeerResponses()
                if (internalErrorsJob == null) internalErrorsJob = collectInternalErrors()
                if (syncUpdateEventsJob == null) syncUpdateEventsJob = collectSyncUpdateEvents()
                if (chatEventsJob == null) chatEventsJob = collectChatEvents()
            }.launchIn(scope)
    }

    private suspend fun registerTagsInHistory() {
        // Has to be one register call per clientId
//        historyInterface.registerTags(tags = listOf(Tags.CHAT_MESSAGE), {}, {})
    }

    private fun collectJsonRpcRequests(): Job = jsonRpcInteractor.clientSyncJsonRpc
        .filter { request -> request.params is ChatParams }
        .onEach { request ->
            when (val params = request.params) {
                is ChatParams.InviteParams -> onInviteRequestUseCase(request, params)
                is ChatParams.MessageParams -> onMessageRequestUseCase(request, params)
                is ChatParams.LeaveParams -> onLeaveRequestUseCase(request)
                is ChatParams.PingParams -> jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS)))
            }
        }.launchIn(scope)

    private fun collectPeerResponses(): Job = jsonRpcInteractor.peerResponse
        .onEach { response ->
            when (response.params) {
                is ChatParams.InviteParams, is CoreChatParams.AcceptanceParams -> onInviteResponseUseCase(response)
                is ChatParams.MessageParams -> onMessageResponseUseCase(response)
                is ChatParams.LeaveParams -> onLeaveResponseUseCase(response)
            }
        }.launchIn(scope)

    private fun collectInternalErrors(): Job = merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow, subscribeToChatTopicsUseCase.errors)
        .onEach { exception -> _events.emit(exception) }
        .launchIn(scope)

    private fun collectSyncUpdateEvents(): Job = syncClient.onSyncUpdateEvents
        .onEach { event -> onSyncUpdateEventUseCase(event) }
        .launchIn(scope)

    private fun collectChatEvents(): Job = merge(onInviteRequestUseCase.events, onMessageRequestUseCase.events, onLeaveRequestUseCase.events, onInviteResponseUseCase.events)
        .onEach { event -> _events.emit(event) }
        .launchIn(scope)
}