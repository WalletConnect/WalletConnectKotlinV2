package com.walletconnect.web3.inbox.push.request

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushWalletInterface
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class GetActiveSubscriptionsRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    val account: AccountId,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Empty>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Empty) =
        respondWithResult(rpc, pushWalletClient.getActiveSubscriptions().filter { it.value.account == account.value }.toResult())

    private fun Map<String, Push.Model.Subscription>.toResult(): Map<String, Web3InboxParams.Response.Push.GetActiveSubscriptionsResult> =
        map { it.key to it.value.toResult() }.toMap()

    private fun Push.Model.Subscription.toResult(): Web3InboxParams.Response.Push.GetActiveSubscriptionsResult =
        Web3InboxParams.Response.Push.GetActiveSubscriptionsResult(topic, account, relay.toResult(), metadata.toResult())

    private fun Push.Model.Subscription.Relay.toResult() =
        Web3InboxParams.RelayParams(protocol, data)

    private fun Core.Model.AppMetaData.toResult()  =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)
}