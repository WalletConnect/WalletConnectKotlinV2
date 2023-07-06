package com.walletconnect.push.common.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.ActiveSubscriptionsQueries
import com.walletconnect.push.common.storage.data.dao.RequestedSubscriptionQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SubscribeStorageRepository(
    private val requestedSubscriptionQueries: RequestedSubscriptionQueries,
    private val activeSubscriptionsQueries: ActiveSubscriptionsQueries,
) {

    suspend fun insertOrAbortRequestedSubscription(
        requestId: Long,
        subscribeTopic: String,
        responseTopic: String,
        account: String,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        expiry: Long,
    ) = withContext(Dispatchers.IO) {
        requestedSubscriptionQueries.insertOrAbortRequestedSubscribtion(
            request_id = requestId,
            subscribe_topic = subscribeTopic,
            response_topic = responseTopic,
            account = account,
            map_of_scope = mapOfScope,
            expiry = expiry,
        )
    }

    suspend fun isAlreadyRequested(account: String, subscribeTopic: String): Boolean = withContext(Dispatchers.IO) {
        requestedSubscriptionQueries.isAlreadyRequested(account, subscribeTopic).executeAsOneOrNull() ?: false
    }

    suspend fun insertOrAbortActiveSubscription(
        account: String,
        updatedExpiry: Long,
        relayProtocol: String,
        relayData: String?,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        dappGeneratedPublicKey: String,
        pushTopic: String,
        requestedSubscriptionRequestId: Long?,
    ) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(account, updatedExpiry, relayProtocol, relayData, mapOfScope, dappGeneratedPublicKey, pushTopic, requestedSubscriptionRequestId)
    }

    suspend fun updateSubscriptionScopeAndJwtByPushTopic(pushTopic: String, updateScope: Map<String, Pair<String, Boolean>>, newExpiry: Long) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.updateSubscriptionScopeAndExpiryByPushTopic(updateScope, newExpiry, pushTopic)
    }

    suspend fun getActiveSubscriptionByPushTopic(pushTopic: String): EngineDO.Subscription.Active? = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getActiveSubscriptionByPushTopic(pushTopic, ::toActiveSubscriptionWithoutMetadata).executeAsOneOrNull()
    }

    suspend fun getAllActiveSubscriptions(): List<EngineDO.Subscription.Active> = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getAllActiveSubscriptions(::toActiveSubscriptionWithoutMetadata).executeAsList()
    }

    suspend fun getRequestedSubscriptionByRequestId(requestId: Long): EngineDO.Subscription.Requested? = withContext(Dispatchers.IO) {
        requestedSubscriptionQueries.getRequestedSubscriptionByRequestId(requestId, ::toRequestedSubscription).executeAsOneOrNull()
    }

    suspend fun deleteSubscriptionByPushTopic(pushTopic: String) = withContext(Dispatchers.IO) {
        val requestedSubscriptionRequestId = activeSubscriptionsQueries.getActiveSubscriptionForeignRequestedSubscriptionIdByPushTopic(pushTopic).executeAsOneOrNull()?.requested_subscription_id
        if (requestedSubscriptionRequestId != null) requestedSubscriptionQueries.deleteByRequestId(requestedSubscriptionRequestId)
        activeSubscriptionsQueries.deleteByPushTopic(pushTopic)
        Timber.d("deleteSubscriptionByPushTopic")
        Timber.d(requestedSubscriptionRequestId.toString())
    }

    private fun toActiveSubscriptionWithoutMetadata(
        account: String,
        expiry: Long,
        relay_protocol: String,
        relay_data: String?,
        map_of_scope: Map<String, Pair<String, Boolean>>,
        dapp_generated_public_key: String,
        push_topic: String,
        requested_subscription_id: Long?,
    ): EngineDO.Subscription.Active = EngineDO.Subscription.Active(
        account = AccountId(account),
        mapOfScope = map_of_scope.map { entry -> entry.key to EngineDO.PushScope.Cached(entry.key, entry.value.first, entry.value.second) }.toMap(),
        expiry = Expiry(expiry),
        relay = RelayProtocolOptions(relay_protocol, relay_data),
        dappGeneratedPublicKey = PublicKey(dapp_generated_public_key),
        pushTopic = Topic(push_topic),
        dappMetaData = null,
        requestedSubscriptionId = requested_subscription_id
    )


    private fun toRequestedSubscription(
        request_id: Long,
        subscribe_topic: String,
        account: String,
        map_of_scope: Map<String, Pair<String, Boolean>>,
        response_topic: String,
        expiry: Long,
    ): EngineDO.Subscription.Requested = EngineDO.Subscription.Requested(
        requestId = request_id,
        responseTopic = Topic(response_topic),
        account = AccountId(account),
        mapOfScope = map_of_scope.map { entry -> entry.key to EngineDO.PushScope.Cached(entry.key, entry.value.first, entry.value.second) }.toMap(),
        expiry = Expiry(expiry),
        subscribeTopic = Topic(subscribe_topic),
    )
}