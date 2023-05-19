package com.walletconnect.web3.inbox.push.event

import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor

internal class OnDeletePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Wallet.Event.Delete>(proxyInteractor) {

    override fun invoke(model: Push.Wallet.Event.Delete) =
        call(Web3InboxRPC.Call.Push.Delete(params = model.toParams()))

    private fun Push.Wallet.Event.Delete.toParams() =
        Web3InboxParams.Call.Push.DeleteParams(topic)
}
