package com.walletconnect.web3.inbox.push.proxy

import com.walletconnect.android.Core
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.proxy.request.ChatRequestUseCase

internal class GetActiveSubscriptionsRequestUseCase(
    private val pushWalletClient: PushWalletInterface,
    proxyInteractor: PushProxyInteractor,
) : PushRequestUseCase<Web3InboxParams.Request.Empty>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Empty) =
        respondWithResult(rpc, pushWalletClient.getActiveSubscriptions().toResult())

    private fun Map<String, Push.Model.Subscription>.toResult(): Map<String, Web3InboxParams.Response.Push.GetActiveSubscriptionsResult> =
        map { it.key to it.value.toResult() }.toMap()

    private fun Push.Model.Subscription.toResult(): Web3InboxParams.Response.Push.GetActiveSubscriptionsResult =
        Web3InboxParams.Response.Push.GetActiveSubscriptionsResult(requestId, topic, account, relay.toResult(), metadata.toResult())

    private fun Push.Model.Subscription.Relay.toResult() =
        Web3InboxParams.RelayParams(protocol, data)

    private fun Core.Model.AppMetaData.toResult()  =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)
}