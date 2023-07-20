package com.walletconnect.web3.inbox.push.request

import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushWalletInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toPush
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class ApproveRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
) : PushRequestUseCase<Web3InboxParams.Request.Push.ApproveParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.ApproveParams) {
        pushWalletClient.approve(
            Push.Params.Approve(params.id, onSign = { onSign(it).toPush() }),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}