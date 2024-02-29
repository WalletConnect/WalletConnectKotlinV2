package com.walletconnect.notify.test.utils.secondary

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.notify.test.utils.globalOnError

open class SecondaryNotifyDelegate : NotifyInterface.Delegate {

    override fun onNotifyNotification(notifyNotification: Notify.Event.Notification) {
    }

    override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
    }

    override fun onError(error: Notify.Model.Error) {
        globalOnError(error)
    }
}