package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor

internal class OnRequestPushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Wallet.Event.Request>(proxyInteractor) {

    override fun invoke(model: Push.Wallet.Event.Request) =
        call(Web3InboxRPC.Call.Push.Request(params = model.toParams()))

    private fun Push.Wallet.Event.Request.toParams() =
        Web3InboxParams.Call.Push.RequestParams(id, metadata.toParams())


    private fun Core.Model.AppMetaData.toParams() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)
}
