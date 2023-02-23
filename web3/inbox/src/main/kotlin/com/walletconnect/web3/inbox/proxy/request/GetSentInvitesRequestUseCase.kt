package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class GetSentInvitesRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.GetSentInvitesParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.GetSentInvitesParams) {
        val invites = chatClient.getSentInvites(Chat.Params.GetSentInvites(Chat.Type.AccountId(params.account)))
        respondWithResult(rpc, invites.toResult())
    }

    private fun Map<Long, Chat.Model.Invite.Sent>.toResult(): List<Web3InboxParams.Response.GetSentInvitesResult> = map { it.value.toResult() }

    private fun Chat.Model.Invite.Sent.toResult(): Web3InboxParams.Response.GetSentInvitesResult =
        Web3InboxParams.Response.GetSentInvitesResult(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, inviteePublicKey, status.toResult())

    //todo: remove once [PR](https://github.com/WalletConnect/walletconnect-docs/pull/500). workaround for js
    private fun Chat.Type.InviteStatus.toResult(): String = when (this) {
        Chat.Type.InviteStatus.APPROVED -> "ACCEPTED"
        else -> this.name
    }.lowercase()
}

