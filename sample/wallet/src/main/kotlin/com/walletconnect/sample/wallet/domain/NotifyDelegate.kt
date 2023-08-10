package com.walletconnect.sample.wallet.domain

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object NotifyDelegate : NotifyClient.Delegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcNotifyEventModels: MutableSharedFlow<Notify.Event?> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wcNotifyEventModels: SharedFlow<Notify.Event?> = _wcNotifyEventModels

    init {
//        NotifyClient.setDelegate(this)
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
        scope.launch { _wcNotifyEventModels.emit(notifyMessage) }
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {
        scope.launch { _wcNotifyEventModels.emit(notifyDelete) }
    }

    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
        scope.launch { _wcNotifyEventModels.emit(notifySubscribe) }
    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {
        scope.launch { _wcNotifyEventModels.emit(notifyUpdate) }
    }

    override fun onError(error: Notify.Model.Error) {
    }
}