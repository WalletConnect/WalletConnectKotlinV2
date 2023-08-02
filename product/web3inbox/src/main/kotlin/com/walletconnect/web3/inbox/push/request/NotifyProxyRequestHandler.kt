@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.request

import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC


internal class NotifyProxyRequestHandler(
    private val subscribeRequestUseCase: SubscribeRequestUseCase,
    private val updateRequestUseCase: UpdateRequestUseCase,
    private val deleteSubscriptionRequestUseCase: DeleteSubscriptionRequestUseCase,
    private val getActiveSubscriptionsRequestUseCase: GetActiveSubscriptionsRequestUseCase,
    private val getMessageHistoryRequestUseCase: GetMessageHistoryRequestUseCase,
    private val deletePushMessageRequestUseCase: DeleteNotifyMessageRequestUseCase,
    private val enableSyncRequestUseCase: EnableSyncRequestUseCase,
) {

    fun handleRequest(rpc: Web3InboxRPC.Request.Notify) {
        when (rpc) {
            is Web3InboxRPC.Request.Notify.Subscribe -> subscribeRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.Update -> updateRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.DeleteSubscription -> deleteSubscriptionRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.GetActiveSubscriptions -> getActiveSubscriptionsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.GetMessageHistory -> getMessageHistoryRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.DeleteNotifyMessage -> deletePushMessageRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.EnableSync -> enableSyncRequestUseCase(rpc, rpc.params)
        }
    }
}