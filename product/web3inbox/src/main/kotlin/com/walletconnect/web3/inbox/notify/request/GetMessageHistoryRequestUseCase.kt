package com.walletconnect.web3.inbox.notify.request

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class GetMessageHistoryRequestUseCase(
    private val notifyClient: NotifyInterface,
    proxyInteractor: NotifyProxyInteractor,
) : NotifyRequestUseCase<Web3InboxParams.Request.Notify.GetMessageHistoryParams>(proxyInteractor) {

    override fun invoke(rpc: Web3InboxRPC, params: Web3InboxParams.Request.Notify.GetMessageHistoryParams) =
        runCatching { notifyClient.getMessageHistory(Notify.Params.MessageHistory(params.topic)) }.fold(
            onSuccess = { result ->
                respondWithResult(
                    rpc,
                    result.toList().sortedByDescending { (_, messageRecord) ->
                        messageRecord.publishedAt
                    }.toMap()
                )
            },
            onFailure = { error -> respondWithError(rpc, Notify.Model.Error(error)) }
        )
}