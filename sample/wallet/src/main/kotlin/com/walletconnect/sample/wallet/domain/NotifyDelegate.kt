package com.walletconnect.sample.wallet.domain

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

object NotifyDelegate : NotifyClient.Delegate {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notifyEvents = MutableSharedFlow<Notify.Event>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    init {
        NotifyClient.setDelegate(this)
    }

    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
        scope.launch {
            Timber.d("NotifyDelegate.onNotifyMessage - $notifyMessage")
            _notifyEvents.emit(notifyMessage)
        }
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {

    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {

    }

    override fun onError(error: Notify.Model.Error) {

    }

    override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
        scope.launch {
            Timber.d("NotifyDelegate.onSubscriptionsChanged - $subscriptionsChanged")
            _notifyEvents.emit(subscriptionsChanged)
        }
    }
}