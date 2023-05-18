package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ChatProxyInteractor

internal class ResolveRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
) : RequestUseCase<Web3InboxParams.Request.Chat.ResolveParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.ResolveParams) {
        chatClient.resolve(Chat.Params.Resolve(Chat.Type.AccountId(params.account)), object : Chat.Listeners.Resolve {
            override fun onError(error: Chat.Model.Error) = respondWithError(rpc, error)
            override fun onSuccess(publicKey: String) = respondWithResult(rpc, publicKey)
        })
    }
}

