package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor

internal class AcceptInviteRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.AcceptParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.AcceptParams) {
        chatClient.accept(
            Chat.Params.Accept(params.id),
            onSuccess = { topic -> respondWithResult(rpc, topic) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}

