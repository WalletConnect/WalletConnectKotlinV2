@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.common.storage.data.dao.MessagesQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class MessagesRepository(private val messagesQueries: MessagesQueries) {

    suspend fun setMessagesForSubscription(
        topic: String,
        messages: List<NotifyRecord>,
    ) {
        messagesQueries.transaction {
            messagesQueries.deleteMessagesByTopic(topic)
            messages.forEach { message ->
                with(message.notifyMessage) {
                    messagesQueries.insertMessage(message.id, topic, message.id, title, body, icon, url, type)
                }
            }
        }
    }

    suspend fun insertMessage(
        requestId: Long,
        topic: String,
        publishedAt: Long,
        title: String,
        body: String,
        icon: String?,
        url: String?,
        type: String,
    ) = withContext(Dispatchers.IO) {
        messagesQueries.insertMessage(requestId, topic, publishedAt, title, body, icon, url, type)
    }

    suspend fun getMessagesByTopic(topic: String): List<NotifyRecord> = withContext(Dispatchers.IO) {
        messagesQueries.getMessagesByTopic(topic, ::mapToMessageRecord).executeAsList()
    }

    suspend fun doesMessagesExistsByRequestId(requestId: Long): Boolean = withContext(Dispatchers.IO) {
        messagesQueries.doesMessagesExistsByRequestId(requestId).executeAsOne()
    }

    suspend fun deleteMessage(requestId: Long) = withContext(Dispatchers.IO) {
        messagesQueries.deleteMessageByRequestId(requestId)
    }

    suspend fun deleteMessagesByTopic(topic: String) = withContext(Dispatchers.IO) {
        messagesQueries.deleteMessagesByTopic(topic)
    }

    suspend fun updateMessageWithPublishedAtByRequestId(publishedAt: Long, requestId: Long) = withContext(Dispatchers.IO) {
        messagesQueries.updateMessageWithPublishedAtByRequestId(publishedAt, requestId)
    }

    private fun mapToMessageRecord(
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
        )
    )
}