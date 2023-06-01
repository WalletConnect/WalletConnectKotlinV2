package com.walletconnect.push.wallet.engine.domain.calls

import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.EngineDO

internal class GetListOfActiveSubscriptionsUseCase(private val subscriptionStorageRepository: SubscriptionStorageRepository): GetListOfActiveSubscriptionsUseCaseInterface {

    override suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription> =
        subscriptionStorageRepository.getAllSubscriptions()
            .filter { subscription -> subscription.subscriptionTopic?.value.isNullOrBlank().not() }
            .associateBy { subscription -> subscription.subscriptionTopic!!.value }
}

internal interface GetListOfActiveSubscriptionsUseCaseInterface {
    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription>
}