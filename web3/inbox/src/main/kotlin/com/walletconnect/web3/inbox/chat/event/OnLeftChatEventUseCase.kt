package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ChatProxyInteractor

internal class OnLeftChatEventUseCase(
    proxyInteractor: ChatProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnLeft>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnLeft) =
        call(Web3InboxRPC.Call.Chat.Leave(params = model.toParams()))

    private fun Chat.Model.Events.OnLeft.toParams() = Web3InboxParams.Call.Chat.LeaveParams(topic)
}

