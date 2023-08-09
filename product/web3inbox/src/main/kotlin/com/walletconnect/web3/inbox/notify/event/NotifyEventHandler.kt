package com.walletconnect.web3.inbox.notify.event

import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient


internal class NotifyEventHandler(
    private val logger: Logger,
    private val onSubscriptionNotifyEventUseCase: OnSubscriptionNotifyEventUseCase,
    private val onUpdateNotifyEventUseCase: OnUpdateNotifyEventUseCase,
    private val onDeleteNotifyEventUseCase: OnDeleteNotifyEventUseCase,
    private val onMessageNotifyEventUseCase: OnMessageNotifyEventUseCase,
) : NotifyClient.Delegate {

    init {
        logger.log("NotifyEventHandler init ")
    }

    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
        logger.log("onNotifySubscription: $notifySubscribe")
        onSubscriptionNotifyEventUseCase(notifySubscribe)
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
        logger.log("onNotifyMessage: $notifyMessage")
        onMessageNotifyEventUseCase(notifyMessage)
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {
        logger.log("onNotifyDelete: $notifyDelete")
        onDeleteNotifyEventUseCase(notifyDelete)
    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {
        logger.log("onNotifyUpdate: $notifyUpdate")
        onUpdateNotifyEventUseCase(notifyUpdate)
    }

    override fun onError(error: Notify.Model.Error) {
        logger.log("NotifyEventHandler.onError: $error")
    }
}