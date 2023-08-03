package com.walletconnect.web3.inbox.push.event

import com.walletconnect.push.client.Push
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnDeleteNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Push.Event.Delete>(proxyInteractor) {

    override fun invoke(model: Push.Event.Delete) =
        call(Web3InboxRPC.Call.Notify.Delete(params = model.toParams()))

    private fun Push.Event.Delete.toParams() =
        Web3InboxParams.Call.Notify.DeleteParams(topic)
}
