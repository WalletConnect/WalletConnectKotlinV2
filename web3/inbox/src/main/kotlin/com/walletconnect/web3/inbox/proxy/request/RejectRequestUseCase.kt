package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class RejectRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.Chat.RejectParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.RejectParams) {
        chatClient.reject(
            Chat.Params.Reject(params.id),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}

