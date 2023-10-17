@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.explorer.domain.usecase.GetNotifyConfigUseCase
import com.walletconnect.notify.common.model.NotificationType

internal class GetNotificationTypesUseCase(
    private val getNotifyConfigUseCase: GetNotifyConfigUseCase,

    ) : GetNotificationTypesUseCaseInterface {

    @Throws(IllegalStateException::class)
    override suspend fun getNotificationTypes(domain: String): Map<String, NotificationType> {
        return getNotifyConfigUseCase(domain).fold(
            onSuccess = { notifyConfig ->
                notifyConfig.types.associate { notificationType ->
                    notificationType.id to NotificationType(
                        name = notificationType.name,
                        id = notificationType.id,
                        description = notificationType.description,
                        isEnabled = true
                    )
                }
            }, onFailure = {
                throw IllegalStateException("Failed to get notify config for domain: $domain")
            }
        )
    }
}

internal interface GetNotificationTypesUseCaseInterface {
    suspend fun getNotificationTypes(domain: String): Map<String, NotificationType>
}