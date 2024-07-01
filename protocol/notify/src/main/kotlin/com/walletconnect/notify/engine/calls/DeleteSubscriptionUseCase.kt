@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.monthInSeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.DeleteSubscription
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.common.model.TimeoutInfo
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.blockingCallsDefaultTimeout
import com.walletconnect.notify.engine.blockingCallsDelayInterval
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.responses.OnDeleteResponseUseCase
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

internal class DeleteSubscriptionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val onDeleteResponseUseCase: OnDeleteResponseUseCase,
) : DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(topic: String, timeout: Duration?): DeleteSubscription = supervisorScope {
        val result = MutableStateFlow<DeleteSubscription>(DeleteSubscription.Processing)
        var timeoutInfo: TimeoutInfo = TimeoutInfo.Nothing
        try {
            val validTimeout = timeout.validateTimeout()
            val activeSubscription: Subscription.Active = subscriptionRepository.getActiveSubscriptionByNotifyTopic(topic)
                ?: throw IllegalStateException("Subscription does not exists for $topic")
            val dappMetaData = metadataStorageRepository.getByTopicAndType(activeSubscription.topic, AppMetaDataType.PEER)
                ?: throw IllegalStateException("Dapp metadata does not exists for $topic")

            val deleteJwt = fetchDidJwtInteractor.deleteRequest(activeSubscription.account, dappMetaData.url, activeSubscription.authenticationPublicKey).getOrThrow()

            val params = CoreNotifyParams.DeleteParams(deleteJwt.value)
            val request = NotifyRpc.NotifyDelete(params = params)
            val irnParams = IrnParams(Tags.NOTIFY_DELETE, Ttl(monthInSeconds))
            timeoutInfo = TimeoutInfo.Data(request.id, Topic(topic))

            onDeleteResponseUseCase.events
                .filter { it.first == params }
                .map { it.second }
                .filter { it is DeleteSubscription.Success || it is DeleteSubscription.Error }
                .onEach { result.emit(it as DeleteSubscription) }
                .launchIn(scope)

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request, onFailure = { error -> result.value = DeleteSubscription.Error(error) })

            withTimeout(validTimeout) {
                while (result.value == DeleteSubscription.Processing) {
                    delay(blockingCallsDelayInterval)
                }
            }

            return@supervisorScope result.value
        } catch (e: TimeoutCancellationException) {
            with(timeoutInfo as TimeoutInfo.Data) {
                return@supervisorScope DeleteSubscription.Error(Throwable("Request: $requestId timed out after ${timeout ?: blockingCallsDefaultTimeout}"))
            }
        } catch (e: Exception) {
            return@supervisorScope DeleteSubscription.Error(e)
        }
    }
}

internal interface DeleteSubscriptionUseCaseInterface {
    suspend fun deleteSubscription(topic: String, timeout: Duration? = null): DeleteSubscription
}