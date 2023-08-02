package com.walletconnect.web3.inbox.push.event

import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal abstract class NotifyEventUseCase<T : Any>(val proxyInteractor: NotifyProxyInteractor) {
    abstract operator fun invoke(model: T)
    fun <T : Web3InboxRPC.Call.Notify> call(call: T) = proxyInteractor.call(call)
}
