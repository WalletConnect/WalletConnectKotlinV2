package com.walletconnect.sample.web3inbox.ui.routes.home.subscriptions.notifications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.web3inbox.ui.routes.accountArg
import com.walletconnect.sample.web3inbox.ui.routes.home.subscriptions.NotifyDelegate
import com.walletconnect.sample.web3inbox.ui.routes.topicArg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val topic = checkNotNull(savedStateHandle.get<String>(topicArg))

    private fun getNotifications() = NotifyClient.getMessageHistory(Notify.Params.MessageHistory(topic)).values.toList()

    val state: MutableStateFlow<NotificationUI> = MutableStateFlow(NotificationUI(getNotifications()))

    init {
        viewModelScope.launch {
            NotifyDelegate.events.collect {
                val notifications = state.value.notifications.toMutableList().apply {
                    when (it) {
                        is Notify.Event.Message -> {
                            add(it.message)
                        }

                        else -> Unit
                    }
                }
                state.value = NotificationUI(notifications.sortedByDescending { it.publishedAt })
            }
        }
    }

}

data class NotificationUI(
    val notifications: List<Notify.Model.MessageRecord>,
)