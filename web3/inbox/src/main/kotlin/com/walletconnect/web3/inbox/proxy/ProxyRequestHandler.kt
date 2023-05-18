@file:JvmSynthetic

package com.walletconnect.web3.inbox.proxy

import android.webkit.JavascriptInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.proxy.request.*
import com.walletconnect.web3.inbox.push.proxy.ApproveRequestUseCase
import com.walletconnect.web3.inbox.push.proxy.GetActiveSubscriptionsRequestUseCase


// TODO: split into push and chat
internal class ProxyRequestHandler(
    private val logger: Logger,
    private val registerRequestUseCase: RegisterRequestUseCase,
    private val getReceivedInvitesRequestUseCase: GetReceivedInvitesRequestUseCase,
    private val getSentInvitesRequestUseCase: GetSentInvitesRequestUseCase,
    private val getThreadsRequestUseCase: GetThreadsRequestUseCase,
    private val getMessagesRequestUseCase: GetMessagesRequestUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val resolveRequestUseCase: ResolveRequestUseCase,
    private val messageRequestUseCase: MessageRequestUseCase,
    private val inviteRequestUseCase: InviteRequestUseCase,
    private val getActiveSubscriptionsRequestUseCase: GetActiveSubscriptionsRequestUseCase,
    private val approveRequestUseCase: ApproveRequestUseCase,
) {

    @JavascriptInterface
    fun postMessage(rpc: String) {
        logger.log("Incoming request from W3I: $rpc")
        val imporvedRPC = if (rpc.contains("getActiveSubscriptions")) rpc.removeSuffix("}") + ",\"params\":{\"account\":\"eip155:1:0xe8e976e38b38fe629597ef6f683f63b6782f3625\"}}" else rpc
        val rpc = Web3InboxSerializer.deserializeRpc(imporvedRPC) ?: return logger.error("Unable to deserialize: $imporvedRPC")
        if (rpc !is Web3InboxRPC.Request) return logger.error("Not a request: $rpc")
        when (rpc) {
            is Web3InboxRPC.Request.Chat.Register -> registerRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetReceivedInvites -> getReceivedInvitesRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetSentInvites -> getSentInvitesRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetThreads -> getThreadsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Accept -> acceptRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Reject -> rejectRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Resolve -> resolveRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.GetMessages -> getMessagesRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Message -> messageRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Chat.Invite -> inviteRequestUseCase(rpc, rpc.params)

            is Web3InboxRPC.Request.Push.GetActiveSubscriptions -> getActiveSubscriptionsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Approve -> approveRequestUseCase(rpc, rpc.params)
        }
    }
}