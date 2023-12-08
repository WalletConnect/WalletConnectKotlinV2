@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.notify.data.storage.NotificationsRepository
import kotlinx.coroutines.supervisorScope

internal class DeleteNotificationUseCase(
    private val notificationsRepository: NotificationsRepository,
) : DeleteNotificationUseCaseInterface {

    override suspend fun deleteNotification(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            notificationsRepository.deleteNotification(requestId)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface DeleteNotificationUseCaseInterface {
    suspend fun deleteNotification(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}