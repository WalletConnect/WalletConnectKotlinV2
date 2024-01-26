@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.SubscriptionRepository

internal class GetActiveSubscriptionsUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
): GetActiveSubscriptionsUseCaseInterface {

    override suspend fun getActiveSubscriptions(accountId: String): Map<String, Subscription.Active> =
        subscriptionRepository.getAccountActiveSubscriptions(AccountId(accountId))
            .map { subscription ->
                val metadata = metadataStorageRepository.getByTopicAndType(subscription.topic, AppMetaDataType.PEER)
                subscription.copy(dappMetaData = metadata)
            }
            .associateBy { subscription -> subscription.topic.value }
}

internal interface GetActiveSubscriptionsUseCaseInterface {
    suspend fun getActiveSubscriptions(accountId: String): Map<String, Subscription.Active>
}