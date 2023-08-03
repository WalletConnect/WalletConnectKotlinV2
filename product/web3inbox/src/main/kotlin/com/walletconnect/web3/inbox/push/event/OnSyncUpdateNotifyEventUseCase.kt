package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnSyncUpdateNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Events.OnSyncUpdate>(proxyInteractor) {

    override fun invoke(model: Events.OnSyncUpdate) = call(Web3InboxRPC.Call.SyncUpdate())
}

