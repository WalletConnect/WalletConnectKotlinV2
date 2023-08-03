@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.data.storage.MessagesRepository
import kotlinx.coroutines.supervisorScope

internal class GetListOfMessagesUseCase(
    private val messagesRepository: MessagesRepository,
): GetListOfMessagesUseCaseInterface {

    override suspend fun getListOfMessages(topic: String): Map<Long, NotifyRecord> = supervisorScope {
        messagesRepository.getMessagesByTopic(topic).map { messageRecord ->
            NotifyRecord(
                id = messageRecord.id,
                topic = messageRecord.topic,
                publishedAt = messageRecord.publishedAt,
                notifyMessage = NotifyMessage(
                    title = messageRecord.notifyMessage.title,
                    body = messageRecord.notifyMessage.body,
                    icon = messageRecord.notifyMessage.icon,
                    url = messageRecord.notifyMessage.url,
                    type = messageRecord.notifyMessage.type,
                )
            )
        }.associateBy { notifyRecord ->
            notifyRecord.id
        }
    }
}

internal interface GetListOfMessagesUseCaseInterface {
    suspend fun getListOfMessages(topic: String): Map<Long, NotifyRecord>
}