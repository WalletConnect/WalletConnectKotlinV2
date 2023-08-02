@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.notify.common.model.EngineDO
import com.walletconnect.notify.data.storage.MessagesRepository
import kotlinx.coroutines.supervisorScope

internal class GetListOfMessagesUseCase(
    private val messagesRepository: MessagesRepository,
): GetListOfMessagesUseCaseInterface {

    override suspend fun getListOfMessages(topic: String): Map<Long, EngineDO.Record> = supervisorScope {
        messagesRepository.getMessagesByTopic(topic).map { messageRecord ->
            EngineDO.Record(
                id = messageRecord.id,
                topic = messageRecord.topic,
                publishedAt = messageRecord.publishedAt,
                message = EngineDO.Message(
                    title = messageRecord.message.title,
                    body = messageRecord.message.body,
                    icon = messageRecord.message.icon,
                    url = messageRecord.message.url,
                    type = messageRecord.message.type,
                )
            )
        }.associateBy { notifyRecord ->
            notifyRecord.id
        }
    }
}

internal interface GetListOfMessagesUseCaseInterface {
    suspend fun getListOfMessages(topic: String): Map<Long, EngineDO.Record>
}