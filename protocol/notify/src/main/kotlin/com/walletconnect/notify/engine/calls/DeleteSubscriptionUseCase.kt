@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.RegisterIdentityAndReturnDidJwtInteractor
import com.walletconnect.notify.engine.sync.use_case.requests.DeleteSubscriptionToNotifySubscriptionStoreUseCase
import kotlinx.coroutines.supervisorScope

internal class DeleteSubscriptionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val messagesRepository: MessagesRepository,
    private val deleteSubscriptionToNotifySubscriptionStore: DeleteSubscriptionToNotifySubscriptionStoreUseCase,
    private val registerIdentityAndReturnDidJwt: RegisterIdentityAndReturnDidJwtInteractor,
) : DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(notifyTopic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
        val activeSubscription: Subscription.Active =
            subscriptionRepository.getActiveSubscriptionByNotifyTopic(notifyTopic) ?: return@supervisorScope onFailure(IllegalStateException("Subscription does not exists for $notifyTopic"))

        val deleteJwt =
            registerIdentityAndReturnDidJwt.deleteRequest(activeSubscription.account, activeSubscription.dappMetaData!!.url, activeSubscription.authenticationPublicKey, onFailure).getOrElse {
                return@supervisorScope onFailure(it)
            }
        val request = NotifyRpc.NotifyDelete(params = NotifyParams.DeleteParams(deleteJwt.value))
        val irnParams = IrnParams(Tags.NOTIFY_DELETE, Ttl(DAY_IN_SECONDS))

        subscriptionRepository.deleteSubscriptionByNotifyTopic(notifyTopic)
        messagesRepository.deleteMessagesByTopic(notifyTopic)

        jsonRpcInteractor.unsubscribe(Topic(notifyTopic))
        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(notifyTopic),
            irnParams,
            request,
            onSuccess = {
                // TODO: Think about if we want to unregister the Echo at all when deleting a subscription, most likely not
                /*CoreClient.Echo.unregister({
                    logger.log("Delete subscription and Echo unregister sent successfully")
                }, {
                    onFailure(it)
                })*/
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