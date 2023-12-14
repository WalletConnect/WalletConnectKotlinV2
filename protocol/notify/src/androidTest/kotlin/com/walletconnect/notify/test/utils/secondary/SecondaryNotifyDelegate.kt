package com.walletconnect.notify.test.utils.secondary

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.notify.test.utils.globalOnError


open class SecondaryNotifyDelegate : NotifyInterface.Delegate {
    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
    }

    override fun onNotifyNotification(notifyNotification: Notify.Event.Notification) {
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {
    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {
    }

    override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
    }

    override fun onError(error: Notify.Model.Error) {
        globalOnError(error)
    }
}