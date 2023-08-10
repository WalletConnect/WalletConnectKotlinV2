package com.walletconnect.web3.inbox.notify.request

import android.net.Uri
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toNotify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class SubscribeRequestUseCase(
    private val notifyClient: NotifyInterface,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.SubscribeParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.SubscribeParams) {
        notifyClient.subscribe(
            Notify.Params.Subscribe(Uri.parse(params.metadata.url), params.account, onSign = { message -> onSign(message).toNotify() }),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}