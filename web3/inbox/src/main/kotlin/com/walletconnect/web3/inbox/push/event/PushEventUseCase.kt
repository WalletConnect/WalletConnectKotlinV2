package com.walletconnect.web3.inbox.push.event

import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor

internal abstract class PushEventUseCase<T : Push.Wallet.Event>(val proxyInteractor: ProxyInteractor) {
    abstract operator fun invoke(model: T)
    fun <T : Web3InboxRPC.Call.Push> call(call: T) = proxyInteractor.call(call)
}
