@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.common.storage.data.dao.NotificationsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NotificationsRepository(private val notificationsQueries: NotificationsQueries) {

    suspend fun insertNotification(
        requestId: Long,
        topic: String,
        publishedAt: Long,
        title: String,
        body: String,
        icon: String?,
        url: String?,
        type: String,
    ) = withContext(Dispatchers.IO) {
        notificationsQueries.insertNotification(requestId, topic, publishedAt, title, body, icon, url, type)
    }

    suspend fun getNotificationsByTopic(topic: String): List<NotifyRecord> = withContext(Dispatchers.IO) {
        notificationsQueries.getNotificationsByTopic(topic, ::mapToNotificationRecordWithoutMetadata).executeAsList()
    }

    suspend fun doesNotificationsExistsByRequestId(requestId: Long): Boolean = withContext(Dispatchers.IO) {
        notificationsQueries.doesNotificationsExistsByRequestId(requestId).executeAsOne()
    }

    suspend fun deleteNotification(requestId: Long) = withContext(Dispatchers.IO) {
        notificationsQueries.deleteNotificationByRequestId(requestId)
    }

    suspend fun deleteNotificationsByTopic(topic: String) = withContext(Dispatchers.IO) {
        notificationsQueries.deleteNotificationsByTopic(topic)
    }

    suspend fun updateNotificationWithPublishedAtByRequestId(publishedAt: Long, requestId: Long) = withContext(Dispatchers.IO) {
        notificationsQueries.updateNotificationWithPublishedAtByRequestId(publishedAt, requestId)
    }

    private fun mapToNotificationRecordWithoutMetadata(
        requestId: Long,
        topic: String,
        publishedAt: Long,
        title: String,
        body: String,
        icon: String?,
        url: String?,
        type: String,
    ): NotifyRecord = NotifyRecord(
        id = requestId,
        topic = topic,
        publishedAt = publishedAt,
        notifyMessage = NotifyMessage(
            title = title,
            body = body,
            icon = icon,
            url = url,
            type = type
        ),
        metadata = null
    )
}