package com.walletconnect.push.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.EngineDO

internal class GetListOfActiveSubscriptionsUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
): GetListOfActiveSubscriptionsUseCaseInterface {

    override suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.Subscription.Active> =
        subscriptionRepository.getAllActiveSubscriptions()
            .map { subscription ->
                val metadata = metadataStorageRepository.getByTopicAndType(subscription.pushTopic, AppMetaDataType.PEER)
                subscription.copy(dappMetaData = metadata)
            }
            .associateBy { subscription -> subscription.pushTopic.value }
}

internal interface GetListOfActiveSubscriptionsUseCaseInterface {
    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.Subscription.Active>
}