package com.walletconnect.push.engine.calls

import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.data.MessagesRepository
import com.walletconnect.util.generateId
import kotlinx.coroutines.supervisorScope

internal class DeleteSubscriptionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val messagesRepository: MessagesRepository,
    private val logger: Logger,
) : DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(pushTopic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
        val request = PushRpc.PushDelete(id = generateId(), params = PushParams.DeleteParams())
        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))

        val activeSubscription: EngineDO.Subscription.Active =
            subscriptionRepository.getActiveSubscriptionByPushTopic(pushTopic) ?: return@supervisorScope onFailure(IllegalStateException("Subscription does not exists for $pushTopic"))

        subscriptionRepository.deleteSubscriptionByPushTopic(pushTopic)
        messagesRepository.deleteMessagesByTopic(pushTopic)

        jsonRpcInteractor.unsubscribe(Topic(pushTopic))
        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(pushTopic), irnParams, request,
            onSuccess = {
                CoreClient.Echo.unregister({
                    logger.log("Delete subscription and Echo unregister sent successfully")
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
    suspend fun deleteSubscription(pushTopic: String, onFailure: (Throwable) -> Unit)
}