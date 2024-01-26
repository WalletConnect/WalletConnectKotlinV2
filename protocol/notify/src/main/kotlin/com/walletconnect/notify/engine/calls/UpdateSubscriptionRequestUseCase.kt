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
import com.walletconnect.android.internal.utils.thirtySeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.UpdateSubscription
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.BLOCKING_CALLS_DELAY_INTERVAL
import com.walletconnect.notify.engine.BLOCKING_CALLS_TIMEOUT
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.responses.OnNotifyUpdateResponseUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

internal class UpdateSubscriptionRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val onNotifyUpdateResponseUseCase: OnNotifyUpdateResponseUseCase,
) : UpdateSubscriptionRequestUseCaseInterface {

    override suspend fun update(notifyTopic: String, scopes: List<String>): UpdateSubscription = supervisorScope {
        try {
            val result = MutableStateFlow<UpdateSubscription>(UpdateSubscription.Processing)

            val subscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(notifyTopic)
                ?: throw IllegalStateException("No subscription found for topic $notifyTopic")
            val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(subscription.notifyTopic, AppMetaDataType.PEER)
                ?: throw IllegalStateException("No metadata found for topic $notifyTopic")
            val didJwt = fetchDidJwtInteractor.updateRequest(subscription.account, metadata.url, subscription.authenticationPublicKey, scopes).getOrThrow()

            val params = CoreNotifyParams.UpdateParams(didJwt.value)
            val request = NotifyRpc.NotifyUpdate(params = params)
            val irnParams = IrnParams(Tags.NOTIFY_UPDATE, Ttl(thirtySeconds))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(notifyTopic), irnParams, request, onFailure = { error -> result.value = UpdateSubscription.Error(error) })

            onNotifyUpdateResponseUseCase.events
                .filter { it.first == params }
                .map { it.second }
                .filter { it is UpdateSubscription.Success || it is UpdateSubscription.Error }
                .onEach { result.emit(it as UpdateSubscription) }
                .launchIn(scope)

            withTimeout(BLOCKING_CALLS_TIMEOUT) {
                while (result.value == UpdateSubscription.Processing) {
                    delay(BLOCKING_CALLS_DELAY_INTERVAL)
                }
            }

            return@supervisorScope result.value
        } catch (e: Exception) {
            return@supervisorScope UpdateSubscription.Error(e)
        }
    }
}

internal interface UpdateSubscriptionRequestUseCaseInterface {
    suspend fun update(notifyTopic: String, scopes: List<String>): UpdateSubscription
}