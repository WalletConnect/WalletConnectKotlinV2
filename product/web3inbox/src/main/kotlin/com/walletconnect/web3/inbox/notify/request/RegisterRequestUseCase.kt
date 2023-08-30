package com.walletconnect.web3.inbox.notify.request

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toNotify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class RegisterRequestUseCase(
    private val notifyClient: NotifyInterface,
    proxyInteractor: NotifyProxyInteractor,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.RegisterParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.RegisterParams) {
        notifyClient.register(
            Notify.Params.Registration(params.account) { message -> onSign(message).toNotify() },
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}