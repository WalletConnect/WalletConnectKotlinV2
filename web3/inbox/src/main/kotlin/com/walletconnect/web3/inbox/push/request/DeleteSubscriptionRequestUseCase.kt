package com.walletconnect.web3.inbox.push.request

import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class DeleteSubscriptionRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Push.DeleteSubscriptionParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.DeleteSubscriptionParams) {
        pushWalletClient.deleteSubscription(Push.Wallet.Params.DeleteSubscription(params.topic), onError = { error -> respondWithError(rpc, error) })
        //todo: add onSuccess to deleteSubscription
        respondWithVoid(rpc)
    }
}