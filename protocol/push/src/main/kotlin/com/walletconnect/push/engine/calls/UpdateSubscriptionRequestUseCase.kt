package com.walletconnect.push.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.engine.domain.RegisterIdentityAndReturnDidJwtUseCaseInterface
import com.walletconnect.utils.Empty
import kotlinx.coroutines.supervisorScope

internal class UpdateSubscriptionRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val registerIdentityAndReturnDidJwtUseCase: RegisterIdentityAndReturnDidJwtUseCaseInterface
): UpdateSubscriptionRequestUseCaseInterface {

    override suspend fun update(pushTopic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val subscription = subscriptionRepository.getActiveSubscriptionByPushTopic(pushTopic)
            ?: return@supervisorScope onFailure(Exception("No subscription found for topic $pushTopic"))
        val metadata: AppMetaData? = metadataStorageRepository.getByTopicAndType(subscription.pushTopic, AppMetaDataType.PEER)
        val didJwt = registerIdentityAndReturnDidJwtUseCase(subscription.account, metadata?.url ?: String.Empty, scopes, { null }, onFailure).getOrElse { error ->
            return@supervisorScope onFailure(error)
        }

        val updateParams = PushParams.UpdateParams(didJwt.value)
        val request = PushRpc.PushUpdate(params = updateParams, topic = pushTopic)
        val irnParams = IrnParams(Tags.PUSH_UPDATE, Ttl(DAY_IN_SECONDS))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(pushTopic), irnParams, request, onSuccess = onSuccess, onFailure = onFailure)
    }
}

internal interface UpdateSubscriptionRequestUseCaseInterface {
    suspend fun update(pushTopic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}