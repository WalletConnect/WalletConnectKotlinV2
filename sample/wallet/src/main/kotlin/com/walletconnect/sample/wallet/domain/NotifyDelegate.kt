package com.walletconnect.sample.wallet.domain

import android.util.Log
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotifyDelegate : NotifyClient.Delegate {
    private val _notifyEvents = MutableSharedFlow<Notify.Event>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    init {
        NotifyClient.setDelegate(this)
    }

    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
        when (notifySubscribe) {
            is Notify.Event.Subscription.Result -> _notifyEvents.tryEmit(notifySubscribe)
            is Notify.Event.Subscription.Error -> {} // Log error
        }
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
        Log.e(this@NotifyDelegate::class.simpleName, notifyMessage.toString())
        _notifyEvents.tryEmit(notifyMessage)
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {

    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {

    }

    override fun onError(error: Notify.Model.Error) {

    }

    override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
        _notifyEvents.tryEmit(subscriptionsChanged)
    }
}