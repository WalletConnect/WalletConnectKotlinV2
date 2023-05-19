@file:JvmSynthetic

package com.walletconnect.web3.inbox.proxy

import android.webkit.JavascriptInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.proxy.request.*
import com.walletconnect.web3.inbox.push.proxy.*


// TODO: split into push and chat
internal class ProxyRequestHandler(
    private val logger: Logger,
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
    // chat 👆
    // push 👇
    private val getActiveSubscriptionsRequestUseCase: GetActiveSubscriptionsRequestUseCase,
    private val approveRequestUseCase: ApproveRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val subscribeRequestUseCase: SubscribeRequestUseCase,
    private val updateRequestUseCase: UpdateRequestUseCase,
    private val deleteSubscriptionRequestUseCase: DeleteSubscriptionRequestUseCase,
    private val getMessageHistoryRequestUseCase: GetMessageHistoryRequestUseCase,
    private val deletePushMessageRequestUseCase: DeletePushMessageRequestUseCase,
) {

    @JavascriptInterface
    fun postMessage(rpcAsString: String) {
        logger.log("Incoming request from W3I: $rpcAsString")
        val safeRpc = rpcAsString.ensureParamsAreIncluded()
        val rpc = Web3InboxSerializer.deserializeRpc(safeRpc) ?: return logger.error("Unable to deserialize: $safeRpc")
        if (rpc !is Web3InboxRPC.Request) return logger.error("Not a request: $rpc")
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
            // chat 👆
            // push 👇
            is Web3InboxRPC.Request.Push.GetActiveSubscriptions -> getActiveSubscriptionsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Approve -> approveRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.DeletePushMessage -> deletePushMessageRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.DeleteSubscription -> deleteSubscriptionRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.GetMessageHistory -> getMessageHistoryRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Reject -> rejectRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Subscribe -> subscribeRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Update -> updateRequestUseCase(rpc, rpc.params)
        }
    }

    private fun String.ensureParamsAreIncluded(): String =
        if (!this.contains("params")) this.removeSuffix("}") + ",\"params\":{}}"
        else this
}