package com.walletconnect.web3.inbox.push.event

import com.walletconnect.notify.client.Notify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnDeleteNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Notify.Event.Delete>(proxyInteractor) {

    override fun invoke(model: Notify.Event.Delete) =
        call(Web3InboxRPC.Call.Notify.Delete(params = model.toParams()))

    private fun Notify.Event.Delete.toParams() =
        Web3InboxParams.Call.Notify.DeleteParams(topic)
}
