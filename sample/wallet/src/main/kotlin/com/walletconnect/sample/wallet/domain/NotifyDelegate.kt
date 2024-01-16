package com.walletconnect.sample.wallet.domain

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

object NotifyDelegate : NotifyClient.Delegate {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notifyEvents = MutableSharedFlow<Notify.Event>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    private val _notifyErrors = MutableSharedFlow<Notify.Model.Error>()
    val notifyErrors = _notifyErrors.asSharedFlow()

    init {
        NotifyClient.setDelegate(this)
    }

    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
        scope.launch {
            Timber.d("NotifyDelegate.notifySubscribe - $notifySubscribe")
            _notifyEvents.emit(notifySubscribe)
        }
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
        scope.launch {
            Timber.d("NotifyDelegate.onNotifyMessage - $notifyMessage")
            _notifyEvents.emit(notifyMessage)
        }
    }

    override fun onNotifyNotification(notifyNotification: Notify.Event.Notification) {
        scope.launch {
            Timber.d("NotifyDelegate.onNotifyNotification - $notifyNotification")
            _notifyEvents.emit(notifyNotification)
        }
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {
        scope.launch {
            Timber.d("NotifyDelegate.notifyDelete - $notifyDelete")
            _notifyEvents.emit(notifyDelete)
        }
    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {
        scope.launch {
            Timber.d("NotifyDelegate.notifyUpdate - $notifyUpdate")
            _notifyEvents.emit(notifyUpdate)
        }
    }

    override fun onError(error: Notify.Model.Error) {
        scope.launch {
            Timber.d("NotifyDelegate.onError - $error")
            _notifyErrors.emit(error)
        }
    }

    override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
        scope.launch {
            Timber.d("NotifyDelegate.onSubscriptionsChanged - $subscriptionsChanged")
            _notifyEvents.emit(subscriptionsChanged)
        }
    }
}