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
import com.walletconnect.notify.common.storage.data.dao.RequestedSubscriptionQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SubscriptionRepository(
    private val requestedSubscriptionQueries: RequestedSubscriptionQueries,
    private val activeSubscriptionsQueries: ActiveSubscriptionsQueries,
) {

    suspend fun setActiveSubscriptions(
        account: String,
        subscriptions: List<Subscription.Active>,
    ) {
        activeSubscriptionsQueries.transaction {
            activeSubscriptionsQueries.deleteByAccount(account)
            //todo decide if removing requested is the right thing to do
            requestedSubscriptionQueries.deleteByAccount(account)
            subscriptions.forEach { subscription ->
                with(subscription) {

                    activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(
                        account,
                        authenticationPublicKey.keyAsHex,
                        expiry.seconds,
                        relay.protocol,
                        relay.data,
                        mapOfNotificationScope.mapValues { scope -> Pair(scope.value.description, scope.value.isSelected) },
                        dappGeneratedPublicKey.keyAsHex,
                        notifyTopic.value,
                        requestedSubscriptionId
                    )
                }
            }
        }
    }

    suspend fun insertOrAbortRequestedSubscription(
        requestId: Long,
        subscribeTopic: String,
        responseTopic: String,
        account: String,
        authenticationPublicKey: PublicKey,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        expiry: Long,
    ) = withContext(Dispatchers.IO) {
        requestedSubscriptionQueries.insertOrAbortRequestedSubscribtion(
            request_id = requestId,
            subscribe_topic = subscribeTopic,
            response_topic = responseTopic,
            account = account,
            authentication_public_key = authenticationPublicKey.keyAsHex,
            map_of_scope = mapOfScope,
            expiry = expiry,
        )
    }

    suspend fun isAlreadyRequested(account: String, subscribeTopic: String): Boolean = withContext(Dispatchers.IO) {
        requestedSubscriptionQueries.isAlreadyRequested(account, subscribeTopic).executeAsOneOrNull() ?: false
    }

    suspend fun insertOrAbortActiveSubscription(
        account: String,
        authenticationPublicKey: PublicKey,
        updatedExpiry: Long,
        relayProtocol: String,
        relayData: String?,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        dappGeneratedPublicKey: String,
        notifyTopic: String,
        requestedSubscriptionRequestId: Long?,
    ) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(
            account,
            authenticationPublicKey.keyAsHex,
            updatedExpiry,
            relayProtocol,
            relayData,
            mapOfScope,
            dappGeneratedPublicKey,
            notifyTopic,
            requestedSubscriptionRequestId
        )
    }

    suspend fun upsertOrAbortActiveSubscription(
        account: String,
        authenticationPublicKey: PublicKey,
        updatedExpiry: Long,
        relayProtocol: String,
        relayData: String?,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        dappGeneratedPublicKey: String,
        notifyTopic: String,
        requestedSubscriptionRequestId: Long?,
    ) = withContext(Dispatchers.IO) {
        if (activeSubscriptionsQueries.doesNotifyTopicExist(notifyTopic).executeAsOneOrNull() == true) {
            activeSubscriptionsQueries.updateOrAbortActiveSubscribtion(
                account,
                authenticationPublicKey.keyAsHex,
                updatedExpiry,
                relayProtocol,
                relayData,
                mapOfScope,
                dappGeneratedPublicKey,
                notifyTopic,
                requestedSubscriptionRequestId,
                notifyTopic
            )
        } else {
            activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(
                account,
                authenticationPublicKey.keyAsHex,
                updatedExpiry,
                relayProtocol,
                relayData,
                mapOfScope,
                dappGeneratedPublicKey,
                notifyTopic,
                requestedSubscriptionRequestId
            )
        }
    }

    suspend fun updateSubscriptionScopeAndJwtByNotifyTopic(notifyTopic: String, updateScope: Map<String, Pair<String, Boolean>>, newExpiry: Long) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.updateSubscriptionScopeAndExpiryByNotifyTopic(updateScope, newExpiry, notifyTopic)
    }

    suspend fun getActiveSubscriptionByNotifyTopic(notifyTopic: String): Subscription.Active? = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getActiveSubscriptionByNotifyTopic(notifyTopic, ::toActiveSubscriptionWithoutMetadata).executeAsOneOrNull()
    }

    suspend fun getAllActiveSubscriptions(): List<Subscription.Active> = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getAllActiveSubscriptions(::toActiveSubscriptionWithoutMetadata).executeAsList()
    }

    suspend fun getRequestedSubscriptionByRequestId(requestId: Long): Subscription.Requested? = withContext(Dispatchers.IO) {
        requestedSubscriptionQueries.getRequestedSubscriptionByRequestId(requestId, ::toRequestedSubscription).executeAsOneOrNull()
    }

    suspend fun deleteSubscriptionByNotifyTopic(notifyTopic: String, account: String) = withContext(Dispatchers.IO) {
        val requestedSubscriptionRequestId = activeSubscriptionsQueries.getActiveSubscriptionForeignRequestedSubscriptionIdByNotifyTopic(notifyTopic).executeAsOneOrNull()?.requested_subscription_id
        if (requestedSubscriptionRequestId != null) requestedSubscriptionQueries.deleteByAccount(account)
        activeSubscriptionsQueries.deleteByNotifyTopic(notifyTopic)
    }

    @Suppress("LocalVariableName")
    private fun toActiveSubscriptionWithoutMetadata(
        account: String,
        authentication_public_key: String,
        expiry: Long,
        relay_protocol: String,
        relay_data: String?,
        map_of_scope: Map<String, Pair<String, Boolean>>,
        dapp_generated_public_key: String,
        notify_topic: String,
        requested_subscription_id: Long?,
    ): Subscription.Active = Subscription.Active(
        account = AccountId(account),
        authenticationPublicKey = PublicKey(authentication_public_key),
        mapOfNotificationScope = map_of_scope.map { entry -> entry.key to NotificationScope.Cached(entry.key, entry.value.first, entry.value.second) }.toMap(),
        expiry = Expiry(expiry),
        relay = RelayProtocolOptions(relay_protocol, relay_data),
        dappGeneratedPublicKey = PublicKey(dapp_generated_public_key),
        notifyTopic = Topic(notify_topic),
        dappMetaData = null,
        requestedSubscriptionId = requested_subscription_id
    )

    @Suppress("LocalVariableName")
    private fun toRequestedSubscription(
        request_id: Long,
        subscribe_topic: String,
        account: String,
        authentication_public_key: String,
        map_of_scope: Map<String, Pair<String, Boolean>>,
        response_topic: String,
        expiry: Long,
    ): Subscription.Requested = Subscription.Requested(
        requestId = request_id,
        responseTopic = Topic(response_topic),
        account = AccountId(account),
        authenticationPublicKey = PublicKey(authentication_public_key),
        mapOfNotificationScope = map_of_scope.map { entry -> entry.key to NotificationScope.Cached(entry.key, entry.value.first, entry.value.second) }.toMap(),
        expiry = Expiry(expiry),
        subscribeTopic = Topic(subscribe_topic),
    )
}