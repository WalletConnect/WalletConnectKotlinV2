@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.common.storage.data.dao.ActiveSubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SubscriptionRepository(
    private val activeSubscriptionsQueries: ActiveSubscriptionsQueries,
) {

    suspend fun setActiveSubscriptions(
        account: String,
        subscriptions: List<Subscription.Active>,
    ) {
        activeSubscriptionsQueries.transaction {
            activeSubscriptionsQueries.deleteByAccount(account)
            subscriptions.forEach { subscription ->
                with(subscription) {

                    activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(
                        account,
                        authenticationPublicKey.keyAsHex,
                        expiry.seconds,
                        relay.protocol,
                        relay.data,
                        mapOfNotificationScope.mapValues { scope -> Triple(scope.value.name, scope.value.description, scope.value.isSelected) },
                        dappGeneratedPublicKey.keyAsHex,
                        notifyTopic.value,
                        requestedSubscriptionId
                    )
                }
            }
        }
    }

    suspend fun updateSubscriptionScopeAndJwtByNotifyTopic(notifyTopic: String, updateScope: Map<String, Triple<String, String, Boolean>>, newExpiry: Long) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.updateSubscriptionScopeAndExpiryByNotifyTopic(updateScope, newExpiry, notifyTopic)
    }

    suspend fun getActiveSubscriptionByNotifyTopic(notifyTopic: String): Subscription.Active? = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getActiveSubscriptionByNotifyTopic(notifyTopic, ::toActiveSubscriptionWithoutMetadata).executeAsOneOrNull()
    }

    suspend fun getAllActiveSubscriptions(): List<Subscription.Active> = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getAllActiveSubscriptions(::toActiveSubscriptionWithoutMetadata).executeAsList()
    }

    suspend fun getAccountActiveSubscriptions(accountId: AccountId): List<Subscription.Active> = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getActiveSubscriptionsByAccount(accountId.value, ::toActiveSubscriptionWithoutMetadata).executeAsList()
    }

    suspend fun deleteSubscriptionByNotifyTopic(notifyTopic: String) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.deleteByNotifyTopic(notifyTopic)
    }

    @Suppress("LocalVariableName")
    private fun toActiveSubscriptionWithoutMetadata(
        account: String,
        authentication_public_key: String,
        expiry: Long,
        relay_protocol: String,
        relay_data: String?,
        map_of_scope: Map<String, Triple<String, String, Boolean>>,
        dapp_generated_public_key: String,
        notify_topic: String,
        requested_subscription_id: Long?,
    ): Subscription.Active = Subscription.Active(
        account = AccountId(account),
        authenticationPublicKey = PublicKey(authentication_public_key),
        mapOfNotificationScope = map_of_scope.map { entry ->
            entry.key to NotificationScope.Cached(
                id = entry.key,
                name = entry.value.first,
                description = entry.value.second,
                isSelected = entry.value.third
            )
        }.toMap(),
        expiry = Expiry(expiry),
        relay = RelayProtocolOptions(relay_protocol, relay_data),
        dappGeneratedPublicKey = PublicKey(dapp_generated_public_key),
        notifyTopic = Topic(notify_topic),
        dappMetaData = null,
        requestedSubscriptionId = requested_subscription_id
    )
}