package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ChatProxyInteractor

internal class GetReceivedInvitesRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.GetReceivedInvitesParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.GetReceivedInvitesParams) =
        runCatching { chatClient.getReceivedInvites(Chat.Params.GetReceivedInvites(Chat.Type.AccountId(params.account))) }.fold(
            // Web3Inbox UI expects only Pending Invites on this call
            onSuccess = { result -> respondWithResult(rpc, result.filter { it.value.status == Chat.Type.InviteStatus.PENDING }.toResult()) },
            onFailure = { error -> respondWithError(rpc, Chat.Model.Error(error)) }
        )


    private fun Map<Long, Chat.Model.Invite.Received>.toResult(): List<Web3InboxParams.Response.Chat.GetReceivedInvitesResult> = map { it.value.toResult() }

    private fun Chat.Model.Invite.Received.toResult(): Web3InboxParams.Response.Chat.GetReceivedInvitesResult =
        Web3InboxParams.Response.Chat.GetReceivedInvitesResult(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, status.toResult())

    private fun Chat.Type.InviteStatus.toResult(): String = name.lowercase()
}