package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.model.NotificationUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class NotificationsViewModel : ViewModel() {
    private val currentSubscriptionTopic = MutableStateFlow("")

    val test = merge(currentSubscriptionTopic, NotifyDelegate.notifyEvents).map {
        if (currentSubscriptionTopic.value.isBlank()) {
            NotificationsState.Empty
        } else {
            val listOfNotificationUI = NotifyClient.getMessageHistory(params = Notify.Params.MessageHistory(currentSubscriptionTopic.value))
                .values.sortedByDescending { it.publishedAt }
                .map { messageRecord -> messageRecord.toNotifyNotification() }

            if (listOfNotificationUI.isEmpty()) {
                NotificationsState.Empty
            } else {
                NotificationsState.Success(listOfNotificationUI)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NotificationsState.Empty)


    private val notifications = MutableStateFlow<List<NotificationUI>>(listOf())

    fun setSubscriptionTopic(topic: String) {
        currentSubscriptionTopic.update { topic }
    }

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

    private fun MutableStateFlow<List<NotificationUI>>.deleteNotification(notificationUI: NotificationUI) {
        value = value - notificationUI
    }

    private fun Notify.Model.MessageRecord.toNotifyNotification(): NotificationUI =
        NotificationUI(
            id = id,
            topic = topic,
            date = getHumanReadableTime(publishedAt),
            title = "${message.title} $id",
            body = message.body,
            url = (message as? Notify.Model.Message.Decrypted)?.url,
            icon = (message as? Notify.Model.Message.Decrypted)?.icon
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
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days in 2..6 -> daysOfWeek[dayOfWeek - 1] // E.g., "Monday"
            else -> df.format(calendar.time) // Locale-specific date format E.g., 9/15/23 or 15/9/23 based on Timezone
        }
    }
}

sealed class NotificationsState {
    object Empty : NotificationsState()
    data class Success(val notifications: List<NotificationUI>) : NotificationsState()
}