@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.notify.common.model.NotificationMessage
import com.walletconnect.notify.common.model.Notification
import com.walletconnect.notify.common.storage.data.dao.NotificationsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NotificationsRepository(private val notificationsQueries: NotificationsQueries) {

    suspend fun insertOrReplaceNotification(record: Notification) = withContext(Dispatchers.IO) {
        with(record) {
            with(notificationMessage) {
                notificationsQueries.insertOrReplaceNotification(id, topic, sentAt, title, body, icon, url, type)
            }
        }
    }

    suspend fun insertOrReplaceNotifications(records: List<Notification>) = withContext(Dispatchers.IO) {
        records.forEach { record ->
            with(record) {
                with(notificationMessage) {
                    notificationsQueries.insertOrReplaceNotification(id, topic, sentAt, title, body, icon, url, type)
                }
            }
        }
    }

    suspend fun doesNotificationsExistsByNotificationId(notificationId: String): Boolean = withContext(Dispatchers.IO) {
        notificationsQueries.doesNotificationsExistsByNotificationId(notificationId).executeAsOne()
    }

    suspend fun deleteNotificationsByTopic(topic: String) = withContext(Dispatchers.IO) {
        notificationsQueries.deleteNotificationsByTopic(topic)
    }

    private fun mapToNotificationRecordWithoutMetadata(
        id: String,
        topic: String,
        sentAt: Long,
        title: String,
        body: String,
        icon: String?,
        url: String?,
        type: String,
    ): Notification = Notification(
        id = id,
        topic = topic,
        sentAt = sentAt,
        notificationMessage = NotificationMessage(
            title = title,
            body = body,
            icon = icon,
            url = url,
            type = type
        ),
        metadata = null
    )
}