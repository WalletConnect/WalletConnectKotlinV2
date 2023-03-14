package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class GetReceivedInvitesRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.GetReceivedInvitesParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.GetReceivedInvitesParams) {
        val invites: Map<Long, Chat.Model.Invite.Received> = chatClient.getReceivedInvites(Chat.Params.GetReceivedInvites(Chat.Type.AccountId(params.account)))
            .filter { it.value.status == Chat.Type.InviteStatus.PENDING } // Web3Inbox UI expects only Pending Invites on this call
        respondWithResult(rpc, invites.toResult())
    }

    private fun Map<Long, Chat.Model.Invite.Received>.toResult(): List<Web3InboxParams.Response.GetReceivedInvitesResult> = map { it.value.toResult() }

    private fun Chat.Model.Invite.Received.toResult(): Web3InboxParams.Response.GetReceivedInvitesResult =
        Web3InboxParams.Response.GetReceivedInvitesResult(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, inviteePublicKey, status.toResult())

    private fun Chat.Type.InviteStatus.toResult(): String = name.lowercase()
}