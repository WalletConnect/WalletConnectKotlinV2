@file:JvmSynthetic

package com.walletconnect.notify.engine.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.convertToUTF8
import com.walletconnect.notify.common.model.GetNotificationHistory
import com.walletconnect.notify.common.model.Notification
import com.walletconnect.notify.common.model.NotificationMessage
import com.walletconnect.notify.data.jwt.getNotifications.GetNotificationsResponseJwtClaim
import com.walletconnect.notify.data.storage.NotificationsRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnGetNotificationsResponseUseCase(
    private val logger: Logger,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val notificationsRepository: NotificationsRepository,
) {

    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.GetNotificationsParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.GetNotificationsParams, EngineEvent>> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.GetNotificationsParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val auth = (response.result as ChatNotifyResponseAuthParams.Auth).auth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<GetNotificationsResponseJwtClaim>(auth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(wcResponse.topic, AppMetaDataType.PEER)
                        ?: throw IllegalStateException("No metadata found for topic ${wcResponse.topic}")

                    val notifications = responseJwtClaim.notifications.mapIndexed { index, notification ->
                        with(notification) {
                            Notification(
                                id = id, topic = wcResponse.topic.value, sentAt = sentAt, metadata = metadata,
                                notificationMessage = NotificationMessage(title = convertToUTF8(title), body = convertToUTF8(body), icon = icon, url = url, type = type),
                            )
                        }
                    }

                    notificationsRepository.insertOrReplaceNotifications(notifications)
                    if (!responseJwtClaim.hasMore) subscriptionRepository.updateActiveSubscriptionWithLastNotificationId(notifications.lastOrNull()?.id, wcResponse.topic.value)
                    GetNotificationHistory.Success(notifications, responseJwtClaim.hasMore)
                }

                is JsonRpcResponse.JsonRpcError -> GetNotificationHistory.Error(Throwable(response.error.message))
            }
        } catch (e: Exception) {
            logger.error(e)
            GetNotificationHistory.Error(e)
        }

        _events.emit(params to resultEvent)
    }
}