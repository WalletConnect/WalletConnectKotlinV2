package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.web3.inbox.chat.event.ChatEventUseCase
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnSyncUpdateChatEventUseCase(
    proxyInteractor: ChatProxyInteractor,
) : ChatEventUseCase<Events.OnSyncUpdate>(proxyInteractor) {
    override fun invoke(model: Events.OnSyncUpdate) = call(Web3InboxRPC.Call.SyncUpdate())
}

