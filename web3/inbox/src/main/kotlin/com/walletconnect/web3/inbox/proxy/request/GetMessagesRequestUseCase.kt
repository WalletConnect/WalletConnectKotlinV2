package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class GetMessagesRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.GetMessagesParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.GetMessagesParams) {
        val messages: List<Chat.Model.Message> = chatClient.getMessages(Chat.Params.GetMessages(params.topic))
        respondWithResult(rpc, messages.toResult())
    }

    private fun List<Chat.Model.Message>.toResult(): List<Web3InboxParams.Response.GetMessagesResult> = map { it.toResult() }

    private fun Chat.Model.Message.toResult(): Web3InboxParams.Response.GetMessagesResult =
        Web3InboxParams.Response.GetMessagesResult(topic, message.value, authorAccount.value, timestamp, media?.toResult())

    private fun Chat.Model.Media.toResult() = Web3InboxParams.Response.GetMessagesResult.MediaResult(type, data.value)

}

