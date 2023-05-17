package com.walletconnect.web3.inbox.push

import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import com.walletconnect.web3.inbox.push.event.OnRequestPushEventUseCase


internal class PushEventHandler(
    private val logger: Logger,
    private val onRequestPushEventUseCase: OnRequestPushEventUseCase,
) : PushWalletClient.Delegate {

    init {
        logger.log("PushEventHandler init ")
    }

    override fun onPushRequest(pushRequest: Push.Wallet.Event.Request) {
        logger.log("onPushRequest: $pushRequest")
        onRequestPushEventUseCase(pushRequest)
    }

    override fun onPushMessage(pushMessage: Push.Wallet.Event.Message) {
        TODO("Not yet implemented")
    }

    override fun onPushDelete(pushDelete: Push.Wallet.Event.Delete) {
        TODO("Not yet implemented")
    }

    override fun onError(error: Push.Model.Error) {
        logger.log("ChatEventHandler.onError: $error")
    }
}