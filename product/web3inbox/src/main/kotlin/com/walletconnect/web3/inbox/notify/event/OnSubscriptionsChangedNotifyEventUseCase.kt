package com.walletconnect.web3.inbox.notify.event

import com.walletconnect.notify.client.Notify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnSubscriptionsChangedNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Notify.Event.SubscriptionsChanged>(proxyInteractor) {

    override fun invoke(model: Notify.Event.SubscriptionsChanged) =
        call(Web3InboxRPC.Call.Notify.SubscriptionsChanged(params = model.toParams()))

    fun Notify.Event.SubscriptionsChanged.toParams(): Web3InboxParams.Call.Notify.SubscriptionsChangedParams =
        Web3InboxParams.Call.Notify.SubscriptionsChangedParams("js sucks")
}
