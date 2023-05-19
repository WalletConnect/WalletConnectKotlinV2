package com.walletconnect.web3.inbox.push.proxy

import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor

internal class SubscribeRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Push.SubscribeParams>(proxyInteractor) {

    //todo: Implement after notify refactor
    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.SubscribeParams) {
        respondWithError(rpc, Push.Model.Error(Throwable("First merge https://github.com/WalletConnect/WalletConnectKotlinV2/pull/875")))
//        pushWalletClient.sub(
//            Push.Wallet.Params.Sub(params.id, params.reason),
//            onSuccess = { respondWithVoid(rpc) },
//            onError = { error -> respondWithError(rpc, error) }
//        )
    }
}