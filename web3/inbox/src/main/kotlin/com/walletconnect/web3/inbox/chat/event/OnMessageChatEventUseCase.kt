package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class OnMessageChatEventUseCase(
    proxyInteractor: ProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnMessage>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnMessage) =
        call(Web3InboxRPC.Call.Chat.Message(params = model.toParams()))

    private fun Chat.Model.Events.OnMessage.toParams() = with(message) {
        Web3InboxParams.Call.Chat.MessageParams(topic, message.value, authorAccount.value, timestamp, media?.toParams())
    }

    private fun Chat.Model.Media.toParams() = Web3InboxParams.Call.Chat.MessageParams.MediaParams(type, data.value)
}

