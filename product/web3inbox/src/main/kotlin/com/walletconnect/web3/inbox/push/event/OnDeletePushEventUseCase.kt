package com.walletconnect.web3.inbox.push.event

import com.walletconnect.push.client.Push
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnDeletePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Event.Delete>(proxyInteractor) {

    override fun invoke(model: Push.Event.Delete) =
        call(Web3InboxRPC.Call.Push.Delete(params = model.toParams()))

    private fun Push.Event.Delete.toParams() =
        Web3InboxParams.Call.Push.DeleteParams(topic)
}
