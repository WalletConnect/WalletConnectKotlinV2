package com.walletconnect.chat.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.common.model.*
import com.walletconnect.chat.storage.data.dao.MessagesQueries
import com.walletconnect.foundation.common.model.Topic

internal class MessageStorageRepository(private val messages: MessagesQueries) {

    suspend fun insertMessage(message: Message) = with(message) {
        messages.insertOrAbortMessage(
            messageId = messageId, topic = topic.value, message = message.message.value, authorAccount = authorAccount.value,
            timestamp = timestamp, mediaType = media?.type, mediaData = media?.data?.value
        )
    }

    suspend fun deleteMessageByMessageId(messageId: Long) = messages.deleteMessageByMessageId(messageId)

    suspend fun deleteMessagesByTopic(topic: String) = messages.deleteMessagesByTopic(topic)

    suspend fun getMessageByTopic(topic: String): List<Message> = messages.getMessagesByTopic(topic, ::dbToMessage).executeAsList()

    private fun dbToMessage(messageId: Long, topic: String, message: String, authorAccount: String, timestamp: Long, mediaType: String?, mediaData: String?) =
        Message(messageId, Topic(topic), ChatMessage(message), AccountId(authorAccount), timestamp, if (mediaType != null && mediaData != null) Media(mediaType, MediaData(mediaData)) else null)

}
