package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class InviteRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.InviteParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.InviteParams) {
        chatClient.invite(
            Chat.Params.Invite(
                inviterAccount = Chat.Type.AccountId(params.inviterAccount),
                inviteeAccount = Chat.Type.AccountId(params.inviteeAccount),
                message = Chat.Type.InviteMessage(params.message),
                inviteePublicKey = params.inviteePublicKey
            ),
            onSuccess = { id -> respondWithResult(rpc, id) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}

