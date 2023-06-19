package com.walletconnect.web3.inbox.chat.event

import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal abstract class ChatEventUseCase<T : Any>(val proxyInteractor: ChatProxyInteractor) {
    abstract operator fun invoke(model: T)
    fun <T : Web3InboxRPC.Call.Chat> call(call: T) = proxyInteractor.call(call)
}