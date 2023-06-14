package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor

internal class MessageRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.MessageParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.MessageParams) {
        chatClient.message(
            Chat.Params.Message(params.topic, Chat.Type.ChatMessage(params.message)),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}

