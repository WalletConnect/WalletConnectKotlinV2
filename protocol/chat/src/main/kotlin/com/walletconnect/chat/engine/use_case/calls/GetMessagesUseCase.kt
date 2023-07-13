package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.chat.common.model.Message
import com.walletconnect.chat.storage.MessageStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking


internal class GetMessagesUseCase(
    private val messageRepository: MessageStorageRepository,
) : GetMessagesUseCaseInterface {

    override fun getMessages(topic: String): List<Message> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            messageRepository.getMessageByTopic(topic)
        }
    }
}

internal interface GetMessagesUseCaseInterface {
    fun getMessages(topic: String): List<Message>
}