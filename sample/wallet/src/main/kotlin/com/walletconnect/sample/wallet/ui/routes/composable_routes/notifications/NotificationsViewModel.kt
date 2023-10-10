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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Suppress("UNCHECKED_CAST")
class NotificationsViewModelFactory(private val topic: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(topic) as T
    }
}

class NotificationsViewModel(topic: String) : ViewModel() {
    var currentSubscription: MutableStateFlow<ActiveSubscriptionsUI> =
        MutableStateFlow(NotifyClient.getActiveSubscriptions()[topic]?.toUI() ?: throw IllegalStateException("No subscription found for topic $topic"))

    val state = merge(currentSubscription, NotifyDelegate.notifyEvents).map {
        run {
            val listOfNotificationUI = NotifyClient.getMessageHistory(params = Notify.Params.MessageHistory(currentSubscription.value.topic))
                .values.sortedByDescending { it.publishedAt }
                .map { messageRecord -> messageRecord.toNotifyNotification() }

            if (listOfNotificationUI.isEmpty()) {
                NotificationsState.Empty
            } else {
                NotificationsState.Success(listOfNotificationUI)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NotificationsState.Loading)


    private val notifications = MutableStateFlow<List<NotificationUI>>(listOf())

    fun getMessageHistory() {
        val messages = with(NotifyClient) {
            getActiveSubscriptions().flatMap { (topic, _) ->
                getMessageHistory(Notify.Params.MessageHistory(topic)).values
            }
        }.map { messageRecord -> messageRecord.toNotifyNotification() }

        notifications.value = messages
    }

    fun deleteNotification(notificationUI: NotificationUI) {
        NotifyClient.deleteNotifyMessage(
            Notify.Params.DeleteMessage(notificationUI.id.toLong()),
            onSuccess = {
                notifications.deleteNotification(notificationUI)
            },
            onError = {
                getMessageHistory()
            }
        )
    }

    fun unsubscribe(onError: (Throwable) -> Unit) {
        NotifyClient.deleteSubscription(
            Notify.Params.DeleteSubscription(
                currentSubscription.value.topic
            ), onError = { onError(it.throwable) }
        )
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
            isUnread = Random.Default.nextBoolean()
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

sealed class NotificationsState() {
    object Loading : NotificationsState()
    object Empty : NotificationsState()
    data class Success(val notifications: List<NotificationUI>) : NotificationsState() //todo: model separation
}