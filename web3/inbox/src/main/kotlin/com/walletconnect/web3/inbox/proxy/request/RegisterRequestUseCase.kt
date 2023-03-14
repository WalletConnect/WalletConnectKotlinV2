package com.walletconnect.web3.inbox.proxy.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toChat
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal class RegisterRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ProxyInteractor,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
) : RequestUseCase<Web3InboxParams.Request.RegisterParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.RegisterParams) {
        chatClient.register(Chat.Params.Register(account = Chat.Type.AccountId(params.account)), object : Chat.Listeners.Register {
            override fun onSuccess(publicKey: String) = respondWithResult(rpc, publicKey)
            override fun onError(error: Chat.Model.Error) = respondWithError(rpc, error)
            override fun onSign(message: String): Chat.Model.Cacao.Signature = this@RegisterRequestUseCase.onSign(message).toChat()
        })
    }
}

