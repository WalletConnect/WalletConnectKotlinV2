@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.notify.data.storage.MessagesRepository
import kotlinx.coroutines.supervisorScope

internal class DeleteNotificationUseCase(
    private val messagesRepository: MessagesRepository,
) : DeleteNotificationUseCaseInterface {

    override suspend fun deleteNotification(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            messagesRepository.deleteMessage(requestId)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface DeleteNotificationUseCaseInterface {
    suspend fun deleteNotification(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}