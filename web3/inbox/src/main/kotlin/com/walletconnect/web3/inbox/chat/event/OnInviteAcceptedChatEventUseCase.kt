package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.util.generateId
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class OnInviteAcceptedChatEventUseCase(
    proxyInteractor: ProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnInviteAccepted>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnInviteAccepted) =
        // todo: remove method override after
        call(Web3InboxRPC.Call.Chat.InviteAccepted(id = generateId(), method = "chat_joined", params = model.toParams()))

    private fun Chat.Model.Events.OnInviteAccepted.toParams() =
        Web3InboxParams.Call.Chat.InviteAcceptedParams(topic)
}

