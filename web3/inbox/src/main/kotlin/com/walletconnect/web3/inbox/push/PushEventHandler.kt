package com.walletconnect.web3.inbox.push

import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient


internal class PushEventHandler(
    private val logger: Logger,
) : PushWalletClient.Delegate {

    init {
        logger.log("PushEventHandler init ")
    }

    override fun onPushRequest(pushRequest: Push.Wallet.Event.Request) {
        TODO("Not yet implemented")
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