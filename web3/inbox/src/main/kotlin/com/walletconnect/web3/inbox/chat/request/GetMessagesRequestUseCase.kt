package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor

internal class GetMessagesRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.GetMessagesParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.GetMessagesParams) =
        runCatching { chatClient.getMessages(Chat.Params.GetMessages(params.topic)) }.fold(
            onSuccess = { result -> respondWithResult(rpc, result.toResult()) },
            onFailure = { error -> respondWithError(rpc, Chat.Model.Error(error)) }
        )


    private fun List<Chat.Model.Message>.toResult(): List<Web3InboxParams.Response.Chat.GetMessagesResult> = map { it.toResult() }

    private fun Chat.Model.Message.toResult(): Web3InboxParams.Response.Chat.GetMessagesResult =
        Web3InboxParams.Response.Chat.GetMessagesResult(topic, message.value, authorAccount.value, timestamp, media?.toResult())

    private fun Chat.Model.Media.toResult() = Web3InboxParams.Response.Chat.GetMessagesResult.MediaResult(type, data.value)

}

