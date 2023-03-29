package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.chat.client.Chat
import com.walletconnect.util.generateClientToClientId
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class OnInviteRejectedChatEventUseCase(
    proxyInteractor: ProxyInteractor,
) : ChatEventUseCase<Chat.Model.Events.OnInviteRejected>(proxyInteractor) {

    override fun invoke(model: Chat.Model.Events.OnInviteRejected) =
        call(Web3InboxRPC.Call.Chat.InviteRejected(id = generateClientToClientId(), params = model.toParams()))

    private fun Chat.Model.Events.OnInviteRejected.toParams() =
        Web3InboxParams.Call.Chat.InviteRejectedParams(invite.toParams())

    private fun Chat.Model.Invite.Sent.toParams() =
        Web3InboxParams.Call.Chat.InviteRejectedParams.InviteParams(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, inviteePublicKey, status.toParams())

    private fun Chat.Type.InviteStatus.toParams(): String = name.lowercase()
}

