package com.walletconnect.web3.inbox.notify.request

import android.net.Uri
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class SubscribeRequestUseCase(
    private val notifyClient: NotifyInterface,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.SubscribeParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.SubscribeParams) {
        val url = if (params.metadata.url == "https://notify.gm.walletconnect.com") "https://dev.gm.walletconnect.com" else params.metadata.url
        notifyClient.subscribe(
            Notify.Params.Subscribe(Uri.parse(url), params.account),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}