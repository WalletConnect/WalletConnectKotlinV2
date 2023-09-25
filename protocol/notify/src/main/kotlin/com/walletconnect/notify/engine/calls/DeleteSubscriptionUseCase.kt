@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import kotlinx.coroutines.supervisorScope

internal class DeleteSubscriptionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val messagesRepository: MessagesRepository,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
) : DeleteSubscriptionUseCaseInterface {

    override suspend fun deleteSubscription(notifyTopic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
        val activeSubscription: Subscription.Active = subscriptionRepository.getActiveSubscriptionByNotifyTopic(notifyTopic)
            ?: return@supervisorScope onFailure(IllegalStateException("Subscription does not exists for $notifyTopic"))

        val account = activeSubscription.account.value
        val dappMetaData = metadataStorageRepository.getByTopicAndType(activeSubscription.notifyTopic, AppMetaDataType.PEER)
            ?: return@supervisorScope onFailure(IllegalStateException("Dapp metadata does not exists for $notifyTopic"))

        val deleteJwt = fetchDidJwtInteractor.deleteRequest(activeSubscription.account, dappMetaData.url, activeSubscription.authenticationPublicKey)
            .getOrElse { return@supervisorScope onFailure(it) }

        val request = NotifyRpc.NotifyDelete(params = CoreNotifyParams.DeleteParams(deleteJwt.value), topic = notifyTopic)
        val irnParams = IrnParams(Tags.NOTIFY_DELETE, Ttl(MONTH_IN_SECONDS))

        subscriptionRepository.deleteSubscriptionByNotifyTopic(notifyTopic, account)
        messagesRepository.deleteMessagesByTopic(notifyTopic)

        jsonRpcInteractor.unsubscribe(Topic(notifyTopic))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(notifyTopic), irnParams, request, onFailure = { onFailure(it) })
    }
}

internal interface DeleteSubscriptionUseCaseInterface {
    suspend fun deleteSubscription(notifyTopic: String, onFailure: (Throwable) -> Unit)
}