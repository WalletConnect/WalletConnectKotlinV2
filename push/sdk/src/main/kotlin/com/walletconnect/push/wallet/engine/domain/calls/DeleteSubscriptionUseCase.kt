package com.walletconnect.push.wallet.engine.domain.calls

import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.util.generateId
import kotlinx.coroutines.supervisorScope

internal class DeleteSubscriptionUseCase(
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val logger: Logger,
): DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(topic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
        val deleteParams = PushParams.DeleteParams(6000, "User Disconnected")
        val request = PushRpc.PushDelete(id = generateId(), params = deleteParams)
        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))

        subscriptionStorageRepository.deleteSubscription(topic)

        jsonRpcInteractor.unsubscribe(Topic(topic))
        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(topic), irnParams, request,
            onSuccess = {
                CoreClient.Echo.unregister({
                    logger.log("Delete sent successfully")
                }, {
                    onFailure(it)
                })
            },
            onFailure = {
                onFailure(it)
            }
        )
    }
}

internal interface DeleteSubscriptionUseCaseInterface {
    suspend fun deleteSubscription(topic: String, onFailure: (Throwable) -> Unit)
}