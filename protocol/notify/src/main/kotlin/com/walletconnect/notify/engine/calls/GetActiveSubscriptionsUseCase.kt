@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.validateTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal class GetActiveSubscriptionsUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) : GetActiveSubscriptionsUseCaseInterface {

    @Throws(TimeoutCancellationException::class)
    override suspend fun getActiveSubscriptions(accountId: String, timeout: Duration?): Map<String, Subscription.Active> {
        val validTimeout = timeout.validateTimeout()

        return withTimeout(validTimeout) {
            subscriptionRepository.getAccountActiveSubscriptions(AccountId(accountId))
                .map { subscription ->
                    val metadata = metadataStorageRepository.getByTopicAndType(subscription.topic, AppMetaDataType.PEER)
                    subscription.copy(dappMetaData = metadata)
                }
                .associateBy { subscription -> subscription.topic.value }
        }
    }

}

internal interface GetActiveSubscriptionsUseCaseInterface {
    suspend fun getActiveSubscriptions(accountId: String, timeout: Duration?): Map<String, Subscription.Active>
}