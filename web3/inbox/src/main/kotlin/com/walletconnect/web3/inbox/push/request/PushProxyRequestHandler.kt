@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.request

import android.webkit.JavascriptInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.chat.request.*
import com.walletconnect.web3.inbox.chat.request.AcceptInviteRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetMessagesRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetReceivedInvitesRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetSentInvitesRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetThreadsRequestUseCase
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.push.request.*


internal class PushProxyRequestHandler(
    private val getActiveSubscriptionsRequestUseCase: GetActiveSubscriptionsRequestUseCase,
    private val approveRequestUseCase: ApproveRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val subscribeRequestUseCase: SubscribeRequestUseCase,
    private val updateRequestUseCase: UpdateRequestUseCase,
    private val deleteSubscriptionRequestUseCase: DeleteSubscriptionRequestUseCase,
    private val getMessageHistoryRequestUseCase: GetMessageHistoryRequestUseCase,
    private val deletePushMessageRequestUseCase: DeletePushMessageRequestUseCase,
) {

    fun handleRequest(rpc: Web3InboxRPC.Request.Push) {
        when (rpc) {
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
}