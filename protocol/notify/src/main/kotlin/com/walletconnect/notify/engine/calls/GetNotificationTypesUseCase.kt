@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.explorer.domain.usecase.GetNotifyConfigUseCase
import com.walletconnect.notify.common.model.NotificationType
import com.walletconnect.notify.data.storage.SubscriptionRepository

internal class GetNotificationTypesUseCase(
    private val getNotifyConfigUseCase: GetNotifyConfigUseCase,

    ) : GetNotificationTypesUseCaseInterface {
    override suspend fun getNotificationTypes(domain: String): Map<String, NotificationType> {
        val notifyConfig = getNotifyConfigUseCase(domain).getOrThrow()

        return notifyConfig.types.associate { notificationType ->
            notificationType.id to NotificationType(
                name = notificationType.name,
                id = notificationType.id,
                description = notificationType.description,
                isEnabled = true
            )
        }
    }
}

internal interface GetNotificationTypesUseCaseInterface {
    suspend fun getNotificationTypes(domain: String): Map<String, NotificationType>
}