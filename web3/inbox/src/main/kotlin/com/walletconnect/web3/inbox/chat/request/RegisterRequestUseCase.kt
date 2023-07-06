package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toChat
import com.walletconnect.web3.inbox.client.toPush
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class RegisterRequestUseCase(
    private val chatClient: ChatInterface,
    proxyInteractor: ChatProxyInteractor,
    private val onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    private val pushWalletClient: PushWalletInterface,
) : ChatRequestUseCase<Web3InboxParams.Request.Chat.RegisterParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Chat.RegisterParams) {
        chatClient.register(Chat.Params.Register(account = Chat.Type.AccountId(params.account)), object : Chat.Listeners.Register {
            override fun onSuccess(publicKey: String) = respondWithResult(rpc, publicKey)
            override fun onError(error: Chat.Model.Error) = respondWithError(rpc, error)
            override fun onSign(message: String): Chat.Model.Cacao.Signature = this@RegisterRequestUseCase.onSign(message).toChat()
        })


        pushWalletClient.enableSync(Push.Wallet.Params.EnableSync(params.account) { onSign(it).toPush() }, onSuccess = {}, onError = {})
    }
}

