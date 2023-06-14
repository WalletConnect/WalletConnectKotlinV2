package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor

internal class GetSentInvitesRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.GetSentInvitesParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.GetSentInvitesParams) =
        runCatching { chatClient.getSentInvites(Chat.Params.GetSentInvites(Chat.Type.AccountId(params.account))) }.fold(
            onSuccess = { result -> respondWithResult(rpc, result.toResult()) },
            onFailure = { error -> respondWithError(rpc, Chat.Model.Error(error)) }
        )


    private fun Map<Long, Chat.Model.Invite.Sent>.toResult(): List<Web3InboxParams.Response.Chat.GetSentInvitesResult> = map { it.value.toResult() }

    private fun Chat.Model.Invite.Sent.toResult(): Web3InboxParams.Response.Chat.GetSentInvitesResult =
        Web3InboxParams.Response.Chat.GetSentInvitesResult(id, inviterAccount.value, inviteeAccount.value, message.value, inviterPublicKey, status.toResult())

    private fun Chat.Type.InviteStatus.toResult(): String = name.lowercase()
}

