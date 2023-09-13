package com.walletconnect.web3.inbox.notify.request

import android.net.Uri
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.notify.common.model.ensureHttpsPrefix
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class SubscribeRequestUseCase(
    private val notifyClient: NotifyInterface,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.SubscribeParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.SubscribeParams) {
        val url = if (params.appDomain == "notify.gm.walletconnect.com") "dev.gm.walletconnect.com" else params.appDomain
        val httpsUrl = url.ensureHttpsPrefix()
        notifyClient.subscribe(
            Notify.Params.Subscribe(Uri.parse(httpsUrl), params.account),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}