package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class OnInviteAcceptedChatEventUseCase(
    proxyInteractor: ProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnInviteAccepted>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnInviteAccepted) =
        call(Web3InboxRPC.Call.Chat.InviteAccepted(params = model.toParams()))

    private fun Chat.Model.Events.OnInviteAccepted.toParams() =
        Web3InboxParams.Call.Chat.InviteAcceptedParams(topic, invite.toParams())

    private fun Chat.Model.Invite.Sent.toParams() =
        Web3InboxParams.Call.Chat.InviteAcceptedParams.InviteParams(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, inviteePublicKey, status.toParams())

    private fun Chat.Type.InviteStatus.toParams(): String = name.lowercase()

}

