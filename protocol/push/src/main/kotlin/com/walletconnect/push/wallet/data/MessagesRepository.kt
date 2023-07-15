@file:JvmSynthetic

package com.walletconnect.push.wallet.data

import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.MessagesQueries
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

    suspend fun getMessagesByTopic(topic: String): List<EngineDO.PushRecord> = withContext(Dispatchers.IO) {
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
    ): EngineDO.PushRecord = EngineDO.PushRecord(
        id = requestId,
        topic = topic,
        publishedAt = publishedAt,
        message = EngineDO.PushMessage(
            title = title,
            body = body,
            icon = icon,
            url = url,
            type = type
        )
    )
}