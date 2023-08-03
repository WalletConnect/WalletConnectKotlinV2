package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.push.client.Push
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnSubscriptionNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Push.Event.Subscription>(proxyInteractor) {

    override fun invoke(model: Push.Event.Subscription) =
        call(Web3InboxRPC.Call.Notify.Subscription(params = model.toParams()))

    private fun Push.Event.Subscription.toParams() = when (this) {
        is Push.Event.Subscription.Error -> this.toParams()
        is Push.Event.Subscription.Result -> this.toParams()
    }

    private fun Push.Event.Subscription.Error.toParams() =
        Web3InboxParams.Call.Notify.Subscription.ErrorParams(id, reason)

    private fun Push.Event.Subscription.Result.toParams() =
        Web3InboxParams.Call.Notify.Subscription.ResultParams(subscription.toParams())

    private fun Push.Model.Subscription.toParams() =
        Web3InboxParams.SubscriptionParams(topic, account, relay.toParams(), metadata.toParams(), scope.toParams(), expiry)

    private fun Push.Model.Subscription.Relay.toParams() =
        Web3InboxParams.RelayParams(protocol, data)

    private fun Core.Model.AppMetaData.toParams() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)

    private fun Map<Push.Model.Subscription.ScopeName, Push.Model.Subscription.ScopeSetting>.toParams() =
        map { (scopeName, scopeSetting) ->
            scopeName.value to Web3InboxParams.ScopeSettingParams(scopeSetting.description, scopeSetting.enabled)
        }.toMap()
}
