package com.walletconnect.push.wallet.engine.domain.calls

import com.walletconnect.push.wallet.data.MessagesRepository
import kotlinx.coroutines.supervisorScope

internal class DeleteMessageUseCase(private val messagesRepository: MessagesRepository): DeleteMessageUseCaseInterface {

    override suspend fun deleteMessage(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            messagesRepository.deleteMessage(requestId)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface DeleteMessageUseCaseInterface {
    suspend fun deleteMessage(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}

