package com.walletconnect.push.wallet.engine.domain.calls

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.PushRpc
import kotlinx.coroutines.supervisorScope

internal class UpdateUseCase(
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val registerIdentityAndReturnDidJwt: RegisterIdentityAndReturnDidJwtUseCase,
) : UpdateUseCaseInterface {

    override suspend fun update(topic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val subscription = subscriptionStorageRepository.getAllSubscriptions()
            .firstOrNull { subscription -> subscription.subscriptionTopic?.value == topic } ?: return@supervisorScope onFailure(Exception("No subscription found for topic $topic"))
        val didJwt = registerIdentityAndReturnDidJwt(subscription.account, subscription.metadata.url, scopes, { null }, onFailure).getOrElse { error ->
            return@supervisorScope onFailure(error)
        }

        val updateParams = PushParams.UpdateParams(didJwt.value)
        val request = PushRpc.PushUpdate(params = updateParams)
        val irnParams = IrnParams(Tags.PUSH_UPDATE, Ttl(DAY_IN_SECONDS))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request, onSuccess = onSuccess, onFailure = onFailure)
    }
}

internal interface UpdateUseCaseInterface {
    suspend fun update(topic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}