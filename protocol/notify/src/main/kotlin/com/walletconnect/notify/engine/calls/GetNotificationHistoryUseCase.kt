@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.GetNotificationHistory
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.TimeoutInfo
import com.walletconnect.notify.data.storage.NotificationsRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.blockingCallsDefaultTimeout
import com.walletconnect.notify.engine.blockingCallsDelayInterval
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.responses.OnGetNotificationsResponseUseCase
import com.walletconnect.notify.engine.validateTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal class GetNotificationHistoryUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val notificationsRepository: NotificationsRepository,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val onGetNotificationsResponseUseCase: OnGetNotificationsResponseUseCase,
    private val logger: Logger,
) : GetNotificationHistoryUseCaseInterface {

    override suspend fun getNotificationHistory(topic: String, limit: Int?, startingAfter: String?, timeout: Duration?): GetNotificationHistory = supervisorScope {
        val result = MutableStateFlow<GetNotificationHistory>(GetNotificationHistory.Processing)
        var timeoutInfo: TimeoutInfo = TimeoutInfo.Nothing
        try {
            val validTimeout = timeout.validateTimeout()
            val subscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(topic)
                ?: throw IllegalStateException("No subscription found for topic $topic")
            val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(subscription.topic, AppMetaDataType.PEER)
                ?: throw IllegalStateException("No metadata found for topic $topic")

            val parsedLimit = minOf(MAX_LIMIT, limit ?: DEFAULT_LIMIT)

            val sortedStoredNotifications = notificationsRepository.getNotificationsByTopic(topic).map { it.copy(metadata = metadata) }

            if (sortedStoredNotifications.size >= parsedLimit || subscription.reachedEndOfHistory) {
                val indexOfAfter = startingAfter?.let { sortedStoredNotifications.indexOfFirst { it.id == startingAfter } } ?: 0
                val pickedNotifications = sortedStoredNotifications.subList(indexOfAfter, minOf(indexOfAfter + parsedLimit, sortedStoredNotifications.size))

                if (pickedNotifications.size <= parsedLimit) {
                    val hasMore = subscription.idOfLastNotification != pickedNotifications.lastOrNull()?.id
                    return@supervisorScope GetNotificationHistory.Success(pickedNotifications, hasMore)
                }
            }

            val didJwt = fetchDidJwtInteractor.getNotificationsRequest(subscription.account, subscription.authenticationPublicKey, metadata.url, parsedLimit, startingAfter).getOrThrow()

            val params = CoreNotifyParams.GetNotificationsParams(didJwt.value)
            val request = NotifyRpc.NotifyGetNotifications(params = params)
            val irnParams = IrnParams(Tags.NOTIFY_GET_NOTIFICATIONS, Ttl(fiveMinutesInSeconds))
            timeoutInfo = TimeoutInfo.Data(request.id, Topic(topic))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request, onFailure = { error -> result.value = GetNotificationHistory.Error(error) })

            onGetNotificationsResponseUseCase.events
                .filter { it.first == params }
                .map { it.second }
                .filter { it is GetNotificationHistory.Success || it is GetNotificationHistory.Error }
                .onEach { result.emit(it as GetNotificationHistory) }
                .launchIn(scope)

            withTimeout(validTimeout) {
                while (result.value == GetNotificationHistory.Processing) {
                    delay(blockingCallsDelayInterval)
                }
            }

            return@supervisorScope result.value
        } catch (e: TimeoutCancellationException) {
            with(timeoutInfo as TimeoutInfo.Data) {
                return@supervisorScope GetNotificationHistory.Error(Throwable("Request: $requestId timed out after ${timeout ?: blockingCallsDefaultTimeout}"))
            }
        } catch (e: Exception) {
            return@supervisorScope GetNotificationHistory.Error(e)
        }
    }
}

private const val MAX_LIMIT = 50
private const val DEFAULT_LIMIT = 10

internal interface GetNotificationHistoryUseCaseInterface {
    suspend fun getNotificationHistory(topic: String, limit: Int?, startingAfter: String?, timeout: Duration? = null): GetNotificationHistory
}