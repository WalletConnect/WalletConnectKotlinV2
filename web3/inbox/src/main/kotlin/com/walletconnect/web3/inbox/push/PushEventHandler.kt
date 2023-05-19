package com.walletconnect.web3.inbox.push

import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import com.walletconnect.web3.inbox.push.event.OnDeletePushEventUseCase
import com.walletconnect.web3.inbox.push.event.OnMessagePushEventUseCase
import com.walletconnect.web3.inbox.push.event.OnRequestPushEventUseCase


internal class PushEventHandler(
    private val logger: Logger,
    private val onRequestPushEventUseCase: OnRequestPushEventUseCase,
    private val onMessagePushEventUseCase: OnMessagePushEventUseCase,
    private val onDeletePushEventUseCase: OnDeletePushEventUseCase,
) : PushWalletClient.Delegate {

    init {
        logger.log("PushEventHandler init ")
    }

    override fun onPushRequest(pushRequest: Push.Wallet.Event.Request) {
        logger.log("onPushRequest: $pushRequest")
        onRequestPushEventUseCase(pushRequest)
    }

    override fun onPushMessage(pushMessage: Push.Wallet.Event.Message) {
        logger.log("onPushMessage: $pushMessage")
        onMessagePushEventUseCase(pushMessage)
    }

    override fun onPushDelete(pushDelete: Push.Wallet.Event.Delete) {
        logger.log("onPushDelete: $pushDelete")
        onDeletePushEventUseCase(pushDelete)
    }

    override fun onError(error: Push.Model.Error) {
        logger.log("PushEventHandler.onError: $error")
    }
}