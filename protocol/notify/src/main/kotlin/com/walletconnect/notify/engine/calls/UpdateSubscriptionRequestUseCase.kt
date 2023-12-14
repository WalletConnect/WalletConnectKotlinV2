@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import kotlinx.coroutines.supervisorScope

internal class UpdateSubscriptionRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
): UpdateSubscriptionRequestUseCaseInterface {

    override suspend fun update(notifyTopic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val subscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(notifyTopic)
            ?: return@supervisorScope onFailure(Exception("No subscription found for topic $notifyTopic"))
        val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(subscription.notifyTopic, AppMetaDataType.PEER)
            ?: return@supervisorScope onFailure(Exception("No metadata found for topic $notifyTopic"))
        val didJwt = fetchDidJwtInteractor.updateRequest(subscription.account, metadata.url, subscription.authenticationPublicKey, scopes).getOrElse { error ->
            return@supervisorScope onFailure(error)
        }

        val updateParams = CoreNotifyParams.UpdateParams(didJwt.value)
        val request = NotifyRpc.NotifyUpdate(params = updateParams)
        val irnParams = IrnParams(Tags.NOTIFY_UPDATE, Ttl(THIRTY_SECONDS))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(notifyTopic), irnParams, request, onSuccess = onSuccess, onFailure = onFailure)
    }
}

internal interface UpdateSubscriptionRequestUseCaseInterface {
    suspend fun update(notifyTopic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}