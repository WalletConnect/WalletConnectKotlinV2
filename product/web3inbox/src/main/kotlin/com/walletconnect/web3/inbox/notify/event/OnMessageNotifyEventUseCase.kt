package com.walletconnect.web3.inbox.notify.event

import com.walletconnect.notify.client.Notify
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

internal class OnMessageNotifyEventUseCase(
    proxyInteractor: NotifyProxyInteractor,
) : NotifyEventUseCase<Notify.Event.Message>(proxyInteractor) {

    override fun invoke(model: Notify.Event.Message) =
        call(Web3InboxRPC.Call.Notify.Message(params = model.toParams()))

    private fun Notify.Event.Message.toParams() =
        Web3InboxParams.Call.Notify.MessageParams(message.toParams())

    private fun Notify.Model.MessageRecord.toParams() = Web3InboxParams.Call.Notify.MessageParams.MessageRecord(id, topic, publishedAt, message.toParams())

    private fun Notify.Model.Message.toParams() =
        when (this) {
            is Notify.Model.Message.Simple -> Web3InboxParams.Call.Notify.MessageParams.Message(title, body, null, null)
            is Notify.Model.Message.Decrypted -> Web3InboxParams.Call.Notify.MessageParams.Message(title, body, icon, url)
        }
}
