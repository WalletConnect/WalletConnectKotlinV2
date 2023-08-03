package com.walletconnect.web3.inbox.notify.request

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class DeleteSubscriptionRequestUseCase(
    private val notifyClient: NotifyInterface,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.DeleteSubscriptionParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.DeleteSubscriptionParams) {
        notifyClient.deleteSubscription(Notify.Params.DeleteSubscription(params.topic), onError = { error -> respondWithError(rpc, error) })
        //todo: add onSuccess to deleteSubscription
        respondWithVoid(rpc)
    }
}