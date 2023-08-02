@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.request

import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC


internal class PushProxyRequestHandler(
    private val getActiveSubscriptionsRequestUseCase: GetActiveSubscriptionsRequestUseCase,
    private val subscribeRequestUseCase: SubscribeRequestUseCase,
    private val updateRequestUseCase: UpdateRequestUseCase,
    private val deleteSubscriptionRequestUseCase: DeleteSubscriptionRequestUseCase,
    private val getMessageHistoryRequestUseCase: GetMessageHistoryRequestUseCase,
    private val deletePushMessageRequestUseCase: DeletePushMessageRequestUseCase,
    private val enableSyncRequestUseCase: EnableSyncRequestUseCase,
) {

    fun handleRequest(rpc: Web3InboxRPC.Request.Push) {
        when (rpc) {
            is Web3InboxRPC.Request.Push.GetActiveSubscriptions -> getActiveSubscriptionsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.DeletePushMessage -> deletePushMessageRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.EnableSync -> enableSyncRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.DeleteSubscription -> deleteSubscriptionRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.GetMessageHistory -> getMessageHistoryRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Reject -> rejectRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Subscribe -> subscribeRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Push.Update -> updateRequestUseCase(rpc, rpc.params)
        }
    }
}