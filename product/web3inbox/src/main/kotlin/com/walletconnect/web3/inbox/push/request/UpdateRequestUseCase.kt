package com.walletconnect.web3.inbox.push.request

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class UpdateRequestUseCase(
    private val notifyClient: NotifyInterface,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.UpdateParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.UpdateParams) {
        notifyClient.update(
            Notify.Params.Update(params.topic, params.scope),
            onSuccess = { respondWithVoid(rpc) },
            onError = { error -> respondWithError(rpc, error) }
        )
    }
}