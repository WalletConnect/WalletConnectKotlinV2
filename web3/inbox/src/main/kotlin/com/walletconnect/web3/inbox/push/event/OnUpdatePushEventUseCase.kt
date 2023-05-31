package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor


internal class OnUpdatePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Wallet.Event.Update>(proxyInteractor) {

    override fun invoke(model: Push.Wallet.Event.Update) =
        call(Web3InboxRPC.Call.Push.Update(params = model.toParams()))

    private fun Push.Wallet.Event.Update.toParams() = when (this) {
        is Push.Wallet.Event.Update.Error -> this.toParams()
        is Push.Wallet.Event.Update.Result -> this.toParams()
    }

    private fun Push.Wallet.Event.Update.Error.toParams() =
        Web3InboxParams.Call.Push.Update.ErrorParams(id, reason)

    private fun Push.Wallet.Event.Update.Result.toParams() =
        Web3InboxParams.Call.Push.Update.ResultParams(subscription.toParams())

    private fun Push.Model.Subscription.toParams() =
        Web3InboxParams.SubscriptionParams(requestId, topic, account, relay.toParams(), metadata.toParams(), scope.toParams(), expiry)

    private fun Push.Model.Subscription.Relay.toParams() =
        Web3InboxParams.RelayParams(protocol, data)

    private fun Core.Model.AppMetaData.toParams() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)

    private fun Map<Push.Model.Subscription.ScopeName, Push.Model.Subscription.ScopeSetting>.toParams() =
        map { (scopeName, scopeSetting) ->
            scopeName.value to Web3InboxParams.ScopeSettingParams(scopeSetting.description, scopeSetting.enabled)
        }.toMap()}
