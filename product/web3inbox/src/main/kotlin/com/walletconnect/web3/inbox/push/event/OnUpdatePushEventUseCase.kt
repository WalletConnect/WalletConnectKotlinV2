package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.push.client.Push
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC


internal class OnUpdatePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Event.Update>(proxyInteractor) {

    override fun invoke(model: Push.Event.Update) =
        call(Web3InboxRPC.Call.Push.Update(params = model.toParams()))

    private fun Push.Event.Update.toParams() = when (this) {
        is Push.Event.Update.Error -> this.toParams()
        is Push.Event.Update.Result -> this.toParams()
    }

    private fun Push.Event.Update.Error.toParams() =
        Web3InboxParams.Call.Push.Update.ErrorParams(id, reason)

    private fun Push.Event.Update.Result.toParams() =
        Web3InboxParams.Call.Push.Update.ResultParams(subscription.toParams())

    private fun Push.Model.Subscription.toParams() =
        Web3InboxParams.SubscriptionParams(topic, account, relay.toParams(), metadata.toParams(), scope.toParams(), expiry)

    private fun Push.Model.Subscription.Relay.toParams() =
        Web3InboxParams.RelayParams(protocol, data)

    private fun Core.Model.AppMetaData.toParams() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)

    private fun Map<Push.Model.Subscription.ScopeName, Push.Model.Subscription.ScopeSetting>.toParams() =
        map { (scopeName, scopeSetting) ->
            scopeName.value to Web3InboxParams.ScopeSettingParams(scopeSetting.description, scopeSetting.enabled)
        }.toMap()}
