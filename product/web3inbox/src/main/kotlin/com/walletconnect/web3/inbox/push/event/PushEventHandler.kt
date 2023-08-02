package com.walletconnect.web3.inbox.push.event

import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushWalletClient


internal class PushEventHandler(
    private val logger: Logger,
    private val onMessagePushEventUseCase: OnMessagePushEventUseCase,
    private val onDeletePushEventUseCase: OnDeletePushEventUseCase,
    private val onSubscriptionPushEventUseCase: OnSubscriptionPushEventUseCase,
    private val onUpdatePushEventUseCase: OnUpdatePushEventUseCase,
) : PushWalletClient.Delegate {

    init {
        logger.log("PushEventHandler init ")
    }

    override fun onPushMessage(pushMessage: Push.Event.Message) {
        logger.log("onPushMessage: $pushMessage")
        onMessagePushEventUseCase(pushMessage)
    }

    override fun onPushDelete(pushDelete: Push.Event.Delete) {
        logger.log("onPushDelete: $pushDelete")
        onDeletePushEventUseCase(pushDelete)
    }

    override fun onPushSubscription(pushSubscribe: Push.Event.Subscription) {
        logger.log("onPushSubscription: $pushSubscribe")
        onSubscriptionPushEventUseCase(pushSubscribe)
    }

    override fun onPushUpdate(pushUpdate: Push.Event.Update) {
        logger.log("onPushUpdate: $pushUpdate")
        onUpdatePushEventUseCase(pushUpdate)
    }

    override fun onError(error: Push.Model.Error) {
        logger.log("PushEventHandler.onError: $error")
    }
}