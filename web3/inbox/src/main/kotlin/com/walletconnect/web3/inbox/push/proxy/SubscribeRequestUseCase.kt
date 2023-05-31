package com.walletconnect.web3.inbox.push.proxy

import android.net.Uri
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toPush
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor

internal class SubscribeRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Push.SubscribeParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Push.SubscribeParams) {
        pushWalletClient.subscribe(
            Push.Wallet.Params.Subscribe(Uri.parse(params.metadata.url), params.account, onSign = { message -> onSign(message).toPush() }),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}