@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.common.model.EngineDO
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.engine.sync.use_case.requests.DeleteSubscriptionToNotifySubscriptionStoreUseCase
import com.walletconnect.util.generateId
import kotlinx.coroutines.supervisorScope

internal class DeleteSubscriptionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val messagesRepository: MessagesRepository,
    private val deleteSubscriptionToNotifySubscriptionStore: DeleteSubscriptionToNotifySubscriptionStoreUseCase,
    private val logger: Logger,
): DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(notifyTopic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
        val request = NotifyRpc.NotifyDelete(id = generateId(), params = NotifyParams.DeleteParams())
        val irnParams = IrnParams(Tags.NOTIFY_DELETE, Ttl(DAY_IN_SECONDS))

        val activeSubscription: EngineDO.Subscription.Active = subscriptionRepository.getActiveSubscriptionByNotifyTopic(notifyTopic) ?: return@supervisorScope onFailure(IllegalStateException("Subscription does not exists for $notifyTopic"))

        subscriptionRepository.deleteSubscriptionByNotifyTopic(notifyTopic)
        messagesRepository.deleteMessagesByTopic(notifyTopic)

        jsonRpcInteractor.unsubscribe(Topic(notifyTopic))
        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(notifyTopic), irnParams, request,
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

        deleteSubscriptionToNotifySubscriptionStore(activeSubscription.account, activeSubscription.notifyTopic, onSuccess = {}, onError = {})
    }

}

internal interface DeleteSubscriptionUseCaseInterface {
    suspend fun deleteSubscription(notifyTopic: String, onFailure: (Throwable) -> Unit)
}