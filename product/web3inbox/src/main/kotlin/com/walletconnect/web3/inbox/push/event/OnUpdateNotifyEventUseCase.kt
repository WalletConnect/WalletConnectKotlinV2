package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.notify.client.Notify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnUpdateNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Notify.Event.Update>(proxyInteractor) {

    override fun invoke(model: Notify.Event.Update) =
        call(Web3InboxRPC.Call.Notify.Update(params = model.toParams()))

    private fun Notify.Event.Update.toParams() = when (this) {
        is Notify.Event.Update.Error -> this.toParams()
        is Notify.Event.Update.Result -> this.toParams()
    }

    private fun Notify.Event.Update.Error.toParams() =
        Web3InboxParams.Call.Notify.Update.ErrorParams(id, reason)

    private fun Notify.Event.Update.Result.toParams() =
        Web3InboxParams.Call.Notify.Update.ResultParams(subscription.toParams())

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
