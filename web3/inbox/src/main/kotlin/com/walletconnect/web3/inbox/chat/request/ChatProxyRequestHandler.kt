@file:JvmSynthetic

package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.web3.inbox.chat.request.*
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC


internal class ChatProxyRequestHandler(
    private val registerRequestUseCase: RegisterRequestUseCase,
    private val getReceivedInvitesRequestUseCase: GetReceivedInvitesRequestUseCase,
    private val getSentInvitesRequestUseCase: GetSentInvitesRequestUseCase,
    private val getThreadsRequestUseCase: GetThreadsRequestUseCase,
    private val getMessagesRequestUseCase: GetMessagesRequestUseCase,
    private val acceptInviteRequestUseCase: AcceptInviteRequestUseCase,
    private val rejectInviteRequestUseCase: RejectInviteRequestUseCase,
    private val resolveRequestUseCase: ResolveRequestUseCase,
    private val messageRequestUseCase: MessageRequestUseCase,
    private val inviteRequestUseCase: InviteRequestUseCase,
) {

    fun handleRequest(rpc: Web3InboxRPC.Request.Chat) {
        when (rpc) {
            is Web3InboxRPC.Request.Chat.Register -> registerRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetReceivedInvites -> getReceivedInvitesRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetSentInvites -> getSentInvitesRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetThreads -> getThreadsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Accept -> acceptInviteRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Reject -> rejectInviteRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Resolve -> resolveRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetMessages -> getMessagesRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Message -> messageRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Invite -> inviteRequestUseCase(rpc, rpc.params)
        }
    }
}