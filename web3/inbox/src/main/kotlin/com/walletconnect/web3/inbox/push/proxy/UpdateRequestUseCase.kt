package com.walletconnect.web3.inbox.push.proxy

import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor

internal class UpdateRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Push.UpdateParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.UpdateParams) {
        respondWithError(rpc, Push.Model.Error(Throwable("First merge https://github.com/WalletConnect/WalletConnectKotlinV2/pull/875")))

//        pushWalletClient.reject(
//            Push.Wallet.Params.Reject(params.id, params.reason),
//            onSuccess = { respondWithVoid(rpc) },
//            onError = { error -> respondWithError(rpc, error) }
//        )
    }
}