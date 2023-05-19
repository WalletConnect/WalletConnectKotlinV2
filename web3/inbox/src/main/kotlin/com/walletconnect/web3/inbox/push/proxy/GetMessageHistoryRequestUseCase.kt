package com.walletconnect.web3.inbox.push.proxy

import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor

internal class GetMessageHistoryRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Push.GetMessageHistoryParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.GetMessageHistoryParams) =
        runCatching { pushWalletClient.getMessageHistory(Push.Wallet.Params.MessageHistory(params.topic)) }.fold(
            onSuccess = { result -> respondWithResult(rpc, result) },
            onFailure = { error -> respondWithError(rpc, Push.Model.Error(error)) }
        )
}