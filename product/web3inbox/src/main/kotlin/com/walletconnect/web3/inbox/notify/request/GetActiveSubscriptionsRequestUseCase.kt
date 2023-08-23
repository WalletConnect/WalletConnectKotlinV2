package com.walletconnect.web3.inbox.notify.request

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class GetActiveSubscriptionsRequestUseCase(
    private val notifyClient: NotifyInterface,
    val account: AccountId,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Empty>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Empty) =
        respondWithResult(rpc, notifyClient.getActiveSubscriptions().filter { it.value.account == account.value }.toResult())

    private fun Map<String, Notify.Model.Subscription>.toResult(): Map<String, Web3InboxParams.Response.Notify.GetActiveSubscriptionsResult> =
        mapValues { (_, value) -> value.toResult() }

    private fun Notify.Model.Subscription.toResult(): Web3InboxParams.Response.Notify.GetActiveSubscriptionsResult =
        Web3InboxParams.Response.Notify.GetActiveSubscriptionsResult(topic, account, relay.toResult(), metadata.toResult(), scope.scopeToResult())

    private fun Notify.Model.Subscription.Relay.toResult() = Web3InboxParams.RelayParams(protocol, data)

    private fun Core.Model.AppMetaData.toResult() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)

    //todo: Cleanup
    private fun Map<Notify.Model.Subscription.ScopeName, Notify.Model.Subscription.ScopeSetting>.scopeToResult() = map {
        it.key.value to Web3InboxParams.ScopeSettingParams(it.value.description, it.value.enabled)
    }.toMap()
}