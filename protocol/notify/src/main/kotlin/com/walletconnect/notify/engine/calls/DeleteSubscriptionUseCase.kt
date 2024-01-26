@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.monthInSeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.DeleteSubscription
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.BLOCKING_CALLS_DELAY_INTERVAL
import com.walletconnect.notify.engine.BLOCKING_CALLS_TIMEOUT
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.responses.OnNotifyDeleteResponseUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

internal class DeleteSubscriptionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val onNotifyDeleteResponseUseCase: OnNotifyDeleteResponseUseCase,
) : DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(notifyTopic: String): DeleteSubscription = supervisorScope {
        try {
            val result = MutableStateFlow<DeleteSubscription>(DeleteSubscription.Processing)

            val activeSubscription: Subscription.Active = subscriptionRepository.getActiveSubscriptionByNotifyTopic(notifyTopic)
                ?: throw IllegalStateException("Subscription does not exists for $notifyTopic")
            val dappMetaData = metadataStorageRepository.getByTopicAndType(activeSubscription.notifyTopic, AppMetaDataType.PEER)
                ?: throw IllegalStateException("Dapp metadata does not exists for $notifyTopic")

            val deleteJwt = fetchDidJwtInteractor.deleteRequest(activeSubscription.account, dappMetaData.url, activeSubscription.authenticationPublicKey).getOrThrow()

            val params = CoreNotifyParams.DeleteParams(deleteJwt.value)
            val request = NotifyRpc.NotifyDelete(params = params)
            val irnParams = IrnParams(Tags.NOTIFY_DELETE, Ttl(monthInSeconds))

            onNotifyDeleteResponseUseCase.events
                .filter { it.first == params }
                .map { it.second }
                .filter { it is DeleteSubscription.Success || it is DeleteSubscription.Error }
                .onEach { result.emit(it as DeleteSubscription) }
                .launchIn(scope)

            jsonRpcInteractor.publishJsonRpcRequest(Topic(notifyTopic), irnParams, request, onFailure = { error -> result.value = DeleteSubscription.Error(error) })

            withTimeout(BLOCKING_CALLS_TIMEOUT) {
                while (result.value == DeleteSubscription.Processing) {
                    delay(BLOCKING_CALLS_DELAY_INTERVAL)
                }
            }

            return@supervisorScope result.value
        } catch (e: Exception) {
            return@supervisorScope DeleteSubscription.Error(e)
        }
    }
}

internal interface DeleteSubscriptionUseCaseInterface {
    suspend fun deleteSubscription(notifyTopic: String): DeleteSubscription
}