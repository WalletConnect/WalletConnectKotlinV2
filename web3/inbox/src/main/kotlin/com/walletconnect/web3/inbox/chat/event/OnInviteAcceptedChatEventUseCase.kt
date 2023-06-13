package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor

internal class OnInviteAcceptedChatEventUseCase(
    proxyInteractor: ChatProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnInviteAccepted>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnInviteAccepted) =
        call(Web3InboxRPC.Call.Chat.InviteAccepted(params = model.toParams()))

    private fun Chat.Model.Events.OnInviteAccepted.toParams() =
        Web3InboxParams.Call.Chat.InviteAcceptedParams(topic, invite.toParams())

    private fun Chat.Model.Invite.Sent.toParams() =
        Web3InboxParams.Call.Chat.InviteParams(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, status.toParams())

    private fun Chat.Type.InviteStatus.toParams(): String = name.lowercase()

}

