package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.Core
import com.walletconnect.push.client.Push
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnProposePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Push.Event.Proposal>(proxyInteractor) {

    override fun invoke(model: Push.Event.Proposal) =
        call(Web3InboxRPC.Call.Push.Propose(params = model.toParams()))

    private fun Push.Event.Proposal.toParams() =
        Web3InboxParams.Call.Push.ProposeParams(id, account, metadata.toParams())

    private fun Core.Model.AppMetaData.toParams() =
        Web3InboxParams.AppMetaDataParams(name, description, url, icons, redirect, verifyUrl)
}
