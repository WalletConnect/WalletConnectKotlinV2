package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnInviteChatEventUseCase(
    proxyInteractor: ChatProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnInvite>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnInvite) =
        call(Web3InboxRPC.Call.Chat.Invite(params = model.toParams()))

    private fun Chat.Model.Events.OnInvite.toParams() = with(invite) {
        Web3InboxParams.Call.Chat.InviteParams(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, status.name.lowercase())
    }
}

