package com.walletconnect.push.wallet.data

import com.walletconnect.push.common.storage.data.dao.MessageQueries

class MessageRepository(private val messageQueries: MessageQueries) {

    fun insertMessage(
        requestId: Long,
        topic: String,
        publishedAt: Long,
        title: String,
        body: String,
        icon: String?,
        url: String?,
    ) {
        messageQueries.insertMessage(requestId, topic, publishedAt, title, body, icon, url)
    }

    fun getMessagesByTopic(topic: String): List<MessageRecord> =
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
    ): MessageRecord =
        MessageRecord(
            requestId,
            topic,
            publishedAt,
            Message(
                title,
                body, icon, url
            )
        )
}