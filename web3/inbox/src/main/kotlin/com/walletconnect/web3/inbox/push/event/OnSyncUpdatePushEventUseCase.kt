package com.walletconnect.web3.inbox.push.event

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnSyncUpdatePushEventUseCase(
    proxyInteractor: PushProxyInteractor,
) : PushEventUseCase<Events.OnSyncUpdate>(proxyInteractor) {

    override fun invoke(model: Events.OnSyncUpdate) =
        call(Web3InboxRPC.Call.SyncUpdate(params = model.toParams()))

    private fun Events.OnSyncUpdate.toParams() = Web3InboxParams.Call.SyncUpdateParams(accountId.value, store.value, update.toParams())

    private fun SyncUpdate.toParams() = when (this) {
        is SyncUpdate.SyncDelete -> Web3InboxParams.Call.SyncUpdateParams.SyncUpdate.SyncDelete(id, key)
        is SyncUpdate.SyncSet -> Web3InboxParams.Call.SyncUpdateParams.SyncUpdate.SyncSet(id, key, value)
    }
}

