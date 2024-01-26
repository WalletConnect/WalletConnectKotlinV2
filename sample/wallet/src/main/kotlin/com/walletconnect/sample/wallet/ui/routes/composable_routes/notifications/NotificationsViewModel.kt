@file:OptIn(FlowPreview::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.model.NotificationUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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
import timber.log.Timber
import java.net.URI
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
        .filterIsInstance<Notify.Event.SubscriptionsChanged>()
        .debounce(500L)
        .map { event -> event.subscriptions.toUI() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NotifyClient.getActiveSubscriptions(Notify.Params.GetActiveSubscriptions(EthAccountDelegate.ethAddress)).values.toList().toUI())

    val currentSubscription: MutableStateFlow<ActiveSubscriptionsUI> =
        MutableStateFlow(_activeSubscriptions.value.firstOrNull { it.topic == topic } ?: throw IllegalStateException("No subscription found for topic $topic"))

    private val notificationTypes by lazy { NotifyClient.getNotificationTypes(Notify.Params.GetNotificationTypes(URI(currentSubscription.value.appDomain).host)).values.toList() }

    private val _notifications = MutableStateFlow<Set<NotificationUI>>(setOf())
    private val _notificationsTrigger = MutableSharedFlow<Unit>(replay = 1)

    val notifications: StateFlow<Set<NotificationUI>> = _notificationsTrigger
        .map { _notifications.value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _notifications.value)

    private val _scrollToTopCounter = MutableStateFlow(0)
    val scrollToTopCounter = _scrollToTopCounter.asStateFlow()

    private val _hasMore = MutableStateFlow<Boolean>(false)
    val hasMore = _hasMore.asStateFlow()

    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.InitialFetching)
    val state = _state.asStateFlow()

    init {
        NotifyDelegate.notifyEvents
            .filterIsInstance<Notify.Event.Notification>()
            .filter { event -> event.notification.topic == topic }
            .onEach { event -> _notifications.addNotification(event.notification.toNotifyNotification()) }
            .onEach { _state.update { NotificationsState.IncomingNotifications } }
            .debounce(500L)
            .onEach { _notificationsTrigger.emit(Unit) }
            .onEach { _state.update { NotificationsState.Success } }
            .onEach { _scrollToTopCounter.update { _scrollToTopCounter.value + 1 } }
            .launchIn(viewModelScope)
    }

    suspend fun fetchRecentNotifications() = viewModelScope.launch(Dispatchers.IO) {
        _state.update { NotificationsState.InitialFetching }
        NotifyClient.getNotificationHistory(params = Notify.Params.GetNotificationHistory(currentSubscription.value.topic)).let { result ->

             val (notifications, state) = when (result) {
                is Notify.Result.GetNotificationHistory.Success -> {
                    Timber.d("fetchAllNotifications: ${result}")

                    _hasMore.value = result.hasMore
                    result.notifications.map { messageRecord -> messageRecord.toNotifyNotification() }.onEach { notification ->

                        Timber.d("fetchAllNotifications: ${notification.id} ${notification.body}")
                    }.toSet() to NotificationsState.Success
                }

                is Notify.Result.GetNotificationHistory.Error -> {
                    Timber.e(result.error.throwable)
                    emptySet<NotificationUI>() to NotificationsState.Failure(result.error.throwable)
                }
            }
            _notifications.value = notifications
            _notificationsTrigger.emit(Unit)
            _state.update { state  }
        }
    }

    fun fetchMore() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { NotificationsState.FetchingMore }

            NotifyClient.getNotificationHistory(params = Notify.Params.GetNotificationHistory(currentSubscription.value.topic, startingAfter = _notifications.value.last().id)).let { result ->
                val (notifications, state) = when (result) {

                    is Notify.Result.GetNotificationHistory.Success -> {
                        Timber.d("fetchMore result: ${result}")

                        _hasMore.value = result.hasMore
                        val resultList = _notifications.value.toMutableList()
                        resultList.addAll(result.notifications.map { messageRecord -> messageRecord.toNotifyNotification() }.onEach { notification ->
                            Timber.d("fetchMore: ${notification.id} ${notification.icon}")
                        })
                        resultList.toSet() to NotificationsState.Success
                    }

                    is Notify.Result.GetNotificationHistory.Error -> {
                        Timber.e(result.error.throwable)
                        _notifications.value to NotificationsState.Failure(result.error.throwable)
                    }
                }
                _notifications.value = notifications
                _notificationsTrigger.emit(Unit)

                _state.update { state  }
            }

        }
    }

    fun retryFetchingAllNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchRecentNotifications()
        }
    }

    fun unsubscribe(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { NotificationsState.Unsubscribing }

            NotifyClient.deleteSubscription(Notify.Params.DeleteSubscription(currentSubscription.value.topic)).let { result ->
                when (result) {
                    is Notify.Result.DeleteSubscription.Success -> onSuccess()


                    is Notify.Result.DeleteSubscription.Error -> {
                        Timber.e(result.error.throwable)
                        onFailure(result.error.throwable)
                        _state.update { NotificationsState.Failure(result.error.throwable) }
                    }
                }
            }
        }
    }

    private fun MutableStateFlow<Set<NotificationUI>>.addNotification(notificationUI: NotificationUI) {
        value = mutableSetOf(notificationUI) + value
        value = value.sortedBy { it.sentAt }.toSet()
    }

    private fun Notify.Model.NotificationRecord.toNotifyNotification(): NotificationUI =
        NotificationUI(
            id = id,
            topic = topic,
            date = getHumanReadableTime(sentAt),
            sentAt = sentAt,
            title = notification.title,
            body = notification.body,
            url = (notification as? Notify.Model.Notification.Decrypted)?.url,
            icon = notificationTypes.find { it.id == (notification as? Notify.Model.Notification.Decrypted)?.type }?.iconUrl ?: metadata.icons.firstOrNull(),
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
    object InitialFetching : NotificationsState
    object FetchingMore : NotificationsState
    object IncomingNotifications : NotificationsState
    object Success : NotificationsState
    data class Failure(val error: Throwable) : NotificationsState
    object Unsubscribing : NotificationsState
}