@file:OptIn(FlowPreview::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.model.NotificationUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@Suppress("UNCHECKED_CAST")
class NotificationsViewModelFactory(private val topic: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(topic) as T
    }
}

class NotificationsViewModel(topic: String) : ViewModel() {
    private val _activeSubscriptions = NotifyDelegate.notifyEvents
        .filter { event ->
            when (event) {
                is Notify.Event.SubscriptionsChanged -> true
                else -> false
            }
        }
        .debounce(500L)
        .map { event ->
            when (event) {
                is Notify.Event.SubscriptionsChanged -> event.subscriptions.toUI()
                else -> throw Throwable("It is simply not possible to hit this exception. I blame bit flip.")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NotifyClient.getActiveSubscriptions().values.toList().toUI())

    val currentSubscription: MutableStateFlow<ActiveSubscriptionsUI> =
        MutableStateFlow(_activeSubscriptions.value.firstOrNull { it.topic == topic } ?: throw IllegalStateException("No subscription found for topic $topic"))

    private val _notifications = MutableStateFlow<List<NotificationUI>>(listOf())
    private val _notificationsTrigger = MutableSharedFlow<Unit>(replay = 1)

    val notifications: StateFlow<List<NotificationUI>> = _notificationsTrigger
        .map { _notifications.value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _notifications.value)

    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Fetching)
    val state = _state.asStateFlow()

    init {
        NotifyDelegate.notifyEvents
            .filterIsInstance<Notify.Event.Message>()
            .filter { event -> event.message.topic == topic }
            .onEach { event -> _notifications.addNotification(event.message.toNotifyNotification()) }
            .onEach { _state.update { NotificationsState.IncomingNotifications }}
            .debounce(500L)
            .onEach { _notificationsTrigger.emit(Unit) }
            .onEach { _state.update { NotificationsState.Success }}
            .launchIn(viewModelScope)
    }

    suspend fun fetchAllNotifications() {
        _state.update { NotificationsState.Fetching }
        _notifications.value = runCatching { getActiveSubscriptionNotifications() }
            .fold(
                onFailure = { error ->
                    _state.update { NotificationsState.Failure(error) }
                    emptyList()
                },
                onSuccess = {
                    _state.update { NotificationsState.Success }
                    it
                }
            )
        _notificationsTrigger.emit(Unit)
    }

    fun retryFetchingAllNotifications() {
        viewModelScope.launch {
            fetchAllNotifications()
        }
    }

    private suspend fun getActiveSubscriptionNotifications(): List<NotificationUI> =
        NotifyClient.getMessageHistory(params = Notify.Params.MessageHistory(currentSubscription.value.topic))
            .values.sortedByDescending { it.publishedAt }
            .map { messageRecord -> messageRecord.toNotifyNotification() }


    fun deleteNotification(notificationUI: NotificationUI) {
        NotifyClient.deleteNotifyMessage(
            Notify.Params.DeleteMessage(notificationUI.id.toLong()),
            onSuccess = {
                _notifications.deleteNotification(notificationUI)
                viewModelScope.launch { _notificationsTrigger.emit(Unit) }
            },
            onError = {
                //todo: onError
            }
        )
    }

    fun unsubscribe(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            _state.update { NotificationsState.Unsubscribing }
            val beforeSubscription = _activeSubscriptions.value

            NotifyClient.deleteSubscription(
                Notify.Params.DeleteSubscription(
                    currentSubscription.value.topic
                ), onSuccess = {
                    viewModelScope.launch {
                        _activeSubscriptions.collect { afterSubscription ->
                            if (beforeSubscription != afterSubscription) {
                                onSuccess()
                                this.cancel()
                            }
                        }
                    }
                }, onError = { error ->
                    onFailure(error.throwable)
                    _state.update { NotificationsState.Failure(error.throwable) }
                }
            )
        }
    }

    private fun MutableStateFlow<List<NotificationUI>>.addNotification(notificationUI: NotificationUI) {
        value = mutableListOf(notificationUI) + value
    }

    private fun MutableStateFlow<List<NotificationUI>>.deleteNotification(notificationUI: NotificationUI) {
        value = value - notificationUI
    }

    private fun Notify.Model.MessageRecord.toNotifyNotification(): NotificationUI =
        NotificationUI(
            id = id,
            topic = topic,
            date = getHumanReadableTime(publishedAt),
            title = message.title,
            body = message.body,
            url = (message as? Notify.Model.Message.Decrypted)?.url,
            icon = (message as? Notify.Model.Message.Decrypted)?.icon,
            isUnread = false,
        )

    private fun getHumanReadableTime(timestampMillis: Long): String {
        val currentTime = System.currentTimeMillis()
        val difference = currentTime - timestampMillis

        // Get the time difference in various units
        val seconds = TimeUnit.MILLISECONDS.toSeconds(difference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
        val hours = TimeUnit.MILLISECONDS.toHours(difference)
        val days = TimeUnit.MILLISECONDS.toDays(difference)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestampMillis

        // Adjusting to the user's timezone
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysOfWeek = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        val df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        df.timeZone = TimeZone.getDefault()

        return when {
            seconds < 60 -> "Now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days == 1L -> "Yesterday"
            days in 2..6 -> daysOfWeek[dayOfWeek - 1] // E.g., "Monday"
            else -> df.format(calendar.time) // Locale-specific date format E.g., 9/15/23 or 15/9/23 based on Timezone
        }
    }
}

sealed interface NotificationsState {
    object Fetching : NotificationsState
    object IncomingNotifications : NotificationsState
    object Success : NotificationsState
    data class Failure(val error: Throwable) : NotificationsState

    object Unsubscribing : NotificationsState
}