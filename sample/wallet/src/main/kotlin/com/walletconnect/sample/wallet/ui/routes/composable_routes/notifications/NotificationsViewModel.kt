package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.model.NotifyNotification
import com.walletconnect.sample.wallet.domain.model.toNotifyNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel : ViewModel() {
    private val notifications = MutableStateFlow<List<NotifyNotification>>(listOf())

    val notifyEvents = NotifyDelegate.wcNotifyEventModels.map { notifyEvent ->
        when (notifyEvent) {
            is Notify.Event.Message -> notifications.addNewNotification(notifyEvent)
            is Notify.Event.Delete -> getMessageHistory()
            else -> Unit
        }
    }.shareIn(viewModelScope, started = SharingStarted.Eagerly)

    val notificationsState = notifications.map {
        if (it.isEmpty()) {
            NotificationsState.Empty
        } else {
            NotificationsState.Success(it)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, NotificationsState.Empty)

    fun getMessageHistory() {
        val messages = with(NotifyClient) {
            getActiveSubscriptions().flatMap { (topic, _) ->
                getMessageHistory(Notify.Params.MessageHistory(topic)).values
            }
        }.map { messageRecord -> messageRecord.toNotifyNotification() }
        notifications.value = messages
    }

    fun deleteNotification(notifyNotification: NotifyNotification) {
        NotifyClient.deleteNotifyMessage(
            Notify.Params.DeleteMessage(notifyNotification.id.toLong()),
            onSuccess = {
                notifications.deleteNotification(notifyNotification)
            },
            onError = {
                getMessageHistory()
            }
        )
    }

    private fun MutableStateFlow<List<NotifyNotification>>.addNewNotification(notifyMessage: Notify.Event.Message) {
        value = listOf(notifyMessage.toNotifyNotification()) + value
    }

    private fun MutableStateFlow<List<NotifyNotification>>.deleteNotification(notifyNotification: NotifyNotification) {
        value = value - notifyNotification
    }
}

sealed class NotificationsState {
    object Empty : NotificationsState()
    data class Success(val notifications: List<NotifyNotification>) : NotificationsState()
}