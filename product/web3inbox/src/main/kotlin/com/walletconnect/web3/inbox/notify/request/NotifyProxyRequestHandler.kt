@file:JvmSynthetic

package com.walletconnect.web3.inbox.notify.request

import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class NotifyProxyRequestHandler(
    private val subscribeRequestUseCase: SubscribeRequestUseCase,
    private val updateRequestUseCase: UpdateRequestUseCase,
    private val deleteSubscriptionRequestUseCase: DeleteSubscriptionRequestUseCase,
    private val getActiveSubscriptionsRequestUseCase: GetActiveSubscriptionsRequestUseCase,
    private val getMessageHistoryRequestUseCase: GetMessageHistoryRequestUseCase,
    private val deleteNotifyMessageRequestUseCase: DeleteNotifyMessageRequestUseCase,
    private val registerRequestUseCase: RegisterRequestUseCase,
    private val logger: Logger,
    ) {

    fun handleRequest(rpc: Web3InboxRPC.Request.Notify) {
        logger.log("Incoming Notify request from W3I: $rpc")
        when (rpc) {
            is Web3InboxRPC.Request.Notify.Subscribe -> subscribeRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.Update -> updateRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.DeleteSubscription -> deleteSubscriptionRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.GetActiveSubscriptions -> getActiveSubscriptionsRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.GetMessageHistory -> getMessageHistoryRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.DeleteNotifyMessage -> deleteNotifyMessageRequestUseCase(rpc, rpc.params)
            is Web3InboxRPC.Request.Notify.Register -> registerRequestUseCase(rpc, rpc.params)
        }
    }
}