package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class GetThreadsRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.GetThreadsParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.GetThreadsParams) {
        val threads: Map<String, Chat.Model.Thread> = chatClient.getThreads(Chat.Params.GetThreads(Chat.Type.AccountId(params.account)))
        respondWithResult(rpc, threads.toResult())
    }

    private fun Map<String, Chat.Model.Thread>.toResult(): List<Web3InboxParams.Response.GetThreadsResult> = map { it.value.toResult() }

    private fun Chat.Model.Thread.toResult(): Web3InboxParams.Response.GetThreadsResult =
        Web3InboxParams.Response.GetThreadsResult(topic, selfAccount.value, peerAccount.value)

}

