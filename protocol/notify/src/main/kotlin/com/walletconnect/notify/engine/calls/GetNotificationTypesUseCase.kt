@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.explorer.domain.usecase.GetNotifyConfigUseCase
import com.walletconnect.notify.common.model.NotificationType
import com.walletconnect.notify.engine.validateTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal class GetNotificationTypesUseCase(
    private val getNotifyConfigUseCase: GetNotifyConfigUseCase,
) : GetNotificationTypesUseCaseInterface {

    @Throws(IllegalStateException::class, TimeoutCancellationException::class)
    override suspend fun getNotificationTypes(domain: String, timeout: Duration?): Map<String, NotificationType> {
        val validTimeout = timeout.validateTimeout()

        return withTimeout(validTimeout) {
            getNotifyConfigUseCase(domain).fold(
                onSuccess = { notifyConfig ->
                    notifyConfig.types.associate { notificationType ->
                        notificationType.id to NotificationType(
                            name = notificationType.name,
                            id = notificationType.id,
                            description = notificationType.description,
                            isEnabled = true,
                            iconUrl = notificationType.imageUrl?.sm
                        )
                    }
                }, onFailure = {
                    throw IllegalStateException("Failed to get notify config for domain: $domain")
                }
            )
        }
    }
}

internal interface GetNotificationTypesUseCaseInterface {
    suspend fun getNotificationTypes(domain: String, timeout: Duration?): Map<String, NotificationType>
}