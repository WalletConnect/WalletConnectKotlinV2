package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC


internal class OnSubscriptionPushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Wallet.Event.Subscription>(proxyInteractor) {

    override fun invoke(model: Push.Wallet.Event.Subscription) =
        call(Web3InboxRPC.Call.Push.Subscription(params = model.toParams()))

    private fun Push.Wallet.Event.Subscription.toParams() = when (this) {
        is Push.Wallet.Event.Subscription.Error -> this.toParams()
        is Push.Wallet.Event.Subscription.Result -> this.toParams()
    }

    private fun Push.Wallet.Event.Subscription.Error.toParams() =
        Web3InboxParams.Call.Push.Subscription.ErrorParams(id, reason)

    private fun Push.Wallet.Event.Subscription.Result.toParams() =
        Web3InboxParams.Call.Push.Subscription.ResultParams(subscription.toParams())

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
