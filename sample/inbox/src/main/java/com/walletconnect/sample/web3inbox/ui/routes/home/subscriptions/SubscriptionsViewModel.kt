package com.walletconnect.sample.web3inbox.ui.routes.home.subscriptions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.sample.web3inbox.ui.routes.accountArg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SubscriptionsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val selectedAccount = checkNotNull(savedStateHandle.get<String>(accountArg))

    fun getSubs() = NotifyClient.getActiveSubscriptions().values.toList()

    val subscriptions: MutableStateFlow<SubscriptionUI> = MutableStateFlow(SubscriptionUI(getSubs()))

    init {
        viewModelScope.launch {
            NotifyDelegate.events.collect {
                val subs = subscriptions.value.sub.toMutableList().apply {
                    when(it) {
                        is Notify.Event.Subscription.Result -> {
                            add(it.subscription)
                        }
                        else -> {}
                    }
                }
                subscriptions.value = SubscriptionUI(subs)
            }
        }
    }

}

data class SubscriptionUI(
    val sub: List<Notify.Model.Subscription>,
)

object NotifyDelegate : NotifyInterface.Delegate {


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val events: MutableSharedFlow<Notify.Event> = MutableSharedFlow()


    override fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription) {
        scope.launch {
            events.emit(notifySubscribe)
        }
    }

    override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
        scope.launch {
            events.emit(notifyMessage)
        }
    }

    override fun onNotifyDelete(notifyDelete: Notify.Event.Delete) {
        scope.launch {
            events.emit(notifyDelete)
        }
    }

    override fun onNotifyUpdate(notifyUpdate: Notify.Event.Update) {
        scope.launch {
            events.emit(notifyUpdate)
        }
    }

    override fun onError(error: Notify.Model.Error) {
        Timber.e(error.throwable)
    }

}