package com.walletconnect.web3.inbox.push.event

import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor

internal abstract class PushEventUseCase<T : Any>(val proxyInteractor: PushProxyInteractor) {
    abstract operator fun invoke(model: T)
    fun <T : Web3InboxRPC.Call.Push> call(call: T) = proxyInteractor.call(call)
}
