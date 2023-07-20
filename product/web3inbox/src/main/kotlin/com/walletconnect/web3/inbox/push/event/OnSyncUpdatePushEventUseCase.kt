package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import timber.log.Timber

internal class OnSyncUpdatePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Events.OnSyncUpdate>(proxyInteractor) {

    override fun invoke(model: Events.OnSyncUpdate) = call(Web3InboxRPC.Call.SyncUpdate())
}

