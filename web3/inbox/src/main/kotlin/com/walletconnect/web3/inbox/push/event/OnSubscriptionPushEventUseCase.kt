package com.walletconnect.web3.inbox.push.event

import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor


//todo First merge https://github.com/WalletConnect/WalletConnectKotlinV2/pull/875
//internal class OnSubscriptionPushEventUseCase(
//    proxyInteractor: PushProxyInteractor,
//) : PushEventUseCase<Push.Wallet.Event.Subscribe>(proxyInteractor) {
//
//    override fun invoke(model: Push.Wallet.Event.Subscribe) =
//        call(Web3InboxRPC.Call.Push.Subscribe(params = model.toParams()))
//
//    private fun Push.Wallet.Event.Subscribe.toParams() =
//        Web3InboxParams.Call.Push.SubscribeParams(id, topic, publishedAt, message.toParams())
//
//
//    private fun Push.Model.Message.toParams() = Web3InboxParams.Call.Push.MessageParams.Message(title, body, icon, url)
//}
