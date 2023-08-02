@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.notify.common.model.EngineDO
import com.walletconnect.notify.common.storage.data.dao.MessagesQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class MessagesRepository(private val messagesQueries: MessagesQueries) {

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

    suspend fun getMessagesByTopic(topic: String): List<EngineDO.Record> = withContext(Dispatchers.IO) {
        messagesQueries.getMessagesByTopic(topic, ::mapToMessageRecord).executeAsList()
    }

    suspend fun deleteMessage(requestId: Long) = withContext(Dispatchers.IO) {
        messagesQueries.deleteMessageByRequestId(requestId)
    }

    suspend fun deleteMessagesByTopic(topic: String) = withContext(Dispatchers.IO) {
        messagesQueries.deleteMessagesByTopic(topic)
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
    ): EngineDO.Record = EngineDO.Record(
        id = requestId,
        topic = topic,
        publishedAt = publishedAt,
        message = EngineDO.Message(
            title = title,
            body = body,
            icon = icon,
            url = url,
            type = type
        )
    )
}