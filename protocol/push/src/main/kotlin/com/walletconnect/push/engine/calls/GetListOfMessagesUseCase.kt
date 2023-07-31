package com.walletconnect.push.engine.calls

import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.data.MessagesRepository
import kotlinx.coroutines.supervisorScope

internal class GetListOfMessagesUseCase(
    private val messagesRepository: MessagesRepository,
): GetListOfMessagesUseCaseInterface {

    override suspend fun getListOfMessages(topic: String): Map<Long, EngineDO.PushRecord> = supervisorScope {
        messagesRepository.getMessagesByTopic(topic).map { messageRecord ->
            EngineDO.PushRecord(
                id = messageRecord.id,
                topic = messageRecord.topic,
                publishedAt = messageRecord.publishedAt,
                message = EngineDO.PushMessage(
                    title = messageRecord.message.title,
                    body = messageRecord.message.body,
                    icon = messageRecord.message.icon,
                    url = messageRecord.message.url,
                    type = messageRecord.message.type,
                )
            )
        }.associateBy { pushRecord ->
            pushRecord.id
        }
    }
}

internal interface GetListOfMessagesUseCaseInterface {
    suspend fun getListOfMessages(topic: String): Map<Long, EngineDO.PushRecord>
}