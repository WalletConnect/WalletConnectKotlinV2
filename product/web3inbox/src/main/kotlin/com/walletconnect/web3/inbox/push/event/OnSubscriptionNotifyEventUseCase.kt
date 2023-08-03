package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.notify.client.Notify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnSubscriptionNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Notify.Event.Subscription>(proxyInteractor) {

    override fun invoke(model: Notify.Event.Subscription) =
        call(Web3InboxRPC.Call.Notify.Subscription(params = model.toParams()))

    private fun Notify.Event.Subscription.toParams() = when (this) {
        is Notify.Event.Subscription.Error -> this.toParams()
        is Notify.Event.Subscription.Result -> this.toParams()
    }

    private fun Notify.Event.Subscription.Error.toParams() =
        Web3InboxParams.Call.Notify.Subscription.ErrorParams(id, reason)

    private fun Notify.Event.Subscription.Result.toParams() =
        Web3InboxParams.Call.Notify.Subscription.ResultParams(subscription.toParams())

    private fun Notify.Model.Subscription.toParams() =
        Web3InboxParams.SubscriptionParams(topic, account, relay.toParams(), metadata.toParams(), scope.toParams(), expiry)

    private fun Notify.Model.Subscription.Relay.toParams() =
        Web3InboxParams.RelayParams(protocol, data)

    private fun Map<Notify.Model.Subscription.ScopeName, Notify.Model.Subscription.ScopeSetting>.toParams() =
        map { (scopeName, scopeSetting) ->
            scopeName.value to Web3InboxParams.ScopeSettingParams(scopeSetting.description, scopeSetting.enabled)
        }.toMap()

    private fun Core.Model.AppMetaData.toParams() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)
}
