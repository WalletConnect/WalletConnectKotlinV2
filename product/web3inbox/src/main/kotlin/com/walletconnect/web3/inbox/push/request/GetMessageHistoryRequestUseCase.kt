package com.walletconnect.web3.inbox.push.request

import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushWalletInterface
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class GetMessageHistoryRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Push.GetMessageHistoryParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.GetMessageHistoryParams) =
        runCatching { pushWalletClient.getMessageHistory(Push.Params.MessageHistory(params.topic)) }.fold(
            onSuccess = { result -> respondWithResult(rpc, result) },
            onFailure = { error -> respondWithError(rpc, Push.Model.Error(error)) }
        )
}