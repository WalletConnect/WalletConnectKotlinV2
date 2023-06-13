package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor

internal class GetThreadsRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.GetThreadsParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.GetThreadsParams) =
        runCatching { chatClient.getThreads(Chat.Params.GetThreads(Chat.Type.AccountId(params.account))) }.fold(
            onSuccess = { result -> respondWithResult(rpc, result.toResult()) },
            onFailure = { error -> respondWithError(rpc, Chat.Model.Error(error)) }
        )

    private fun Map<String, Chat.Model.Thread>.toResult(): List<Web3InboxParams.Response.Chat.GetThreadsResult> = map { it.value.toResult() }

    private fun Chat.Model.Thread.toResult(): Web3InboxParams.Response.Chat.GetThreadsResult =
        Web3InboxParams.Response.Chat.GetThreadsResult(topic, selfAccount.value, peerAccount.value)

}

