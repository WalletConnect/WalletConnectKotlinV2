@file:JvmSynthetic

package com.walletconnect.push.wallet.data

import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.MessageQueries

internal class MessageRepository(private val messageQueries: MessageQueries) {

    fun insertMessage(
        requestId: Long,
        topic: String,
        publishedAt: Long,
        title: String,
        body: String,
        icon: String?,
        url: String?,
        type: String,
    ) {
        messageQueries.insertMessage(requestId, topic, publishedAt, title, body, icon, url, type)
    }

    fun getMessagesByTopic(topic: String): List<EngineDO.PushRecord> =
        messageQueries.getMessagesByTopic(topic, ::mapToMessageRecord).executeAsList()

    fun deleteMessage(requestId: Long) {
        messageQueries.deleteMessageByRequestId(requestId)
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
            title,
            body,
            icon,
            url,
            type
        )
    )
}