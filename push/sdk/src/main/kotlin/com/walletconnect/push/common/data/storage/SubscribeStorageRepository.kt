package com.walletconnect.push.common.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.SubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscribeStorageRepository(private val subscriptionQueries: SubscriptionsQueries) {

    suspend fun insertOrReplaceSubscriptionRequested(
        requestId: Long,
        subscribeTopic: String,
        dappDidPublicKey: String,
        selfPublicKey: String,
        responseTopic: String,
        account: String,
        dappUri: String,
        didJwt: String,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        expiry: Long,
    ) = withContext(Dispatchers.IO) {
        subscriptionQueries.insertOrReplaceSubscribeRequested(
            request_id = requestId,
            subscribe_topic = subscribeTopic,
            dapp_did_public_key = dappDidPublicKey,
            self_public_key = selfPublicKey,
            response_topic = responseTopic,
            account = account,
            dapp_uri = dappUri,
            did_jwt = didJwt,
            map_of_scope = mapOfScope,
            expiry = expiry
        )
    }

    suspend fun getRespondedSubscribeByRequestId(requestId: Long): EngineDO.PushSubscribe.Responded? = withContext(Dispatchers.IO) {
        subscriptionQueries.getSubscribeByRequestId(requestId, ::toSubscription).executeAsOneOrNull() as? EngineDO.PushSubscribe.Responded
    }

    suspend fun getSubscribeByPeerPublicKey(peerPublicKey: String): EngineDO.PushSubscribe.Responded? = withContext(Dispatchers.IO) {
        subscriptionQueries.getSubscribeByDappGeneratedPublicKey(peerPublicKey, ::toSubscription).executeAsOneOrNull() as? EngineDO.PushSubscribe.Responded
    }

    suspend fun updateSubscribeToResponded(requestId: Long, dappGeneratedPublicKey: String, pushTopic: String, updatedExpiry: Long) = withContext(Dispatchers.IO) {
        subscriptionQueries.updateOrReplaceToResponded(dappGeneratedPublicKey, pushTopic, updatedExpiry, requestId)
    }

    suspend fun updateSubscriptionScopeAndJwtByPushTopic(pushTopic: String, updateScope: Map<String, Pair<String, Boolean>>, updateJwt: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        subscriptionQueries.updateSubscriptionScopeAndJwtByPushTopic(updateScope, updateJwt, newExpiry, pushTopic)
    }

    suspend fun getAllSubscriptions(): List<EngineDO.PushSubscribe> = withContext(Dispatchers.IO) {
        subscriptionQueries.getAllSubscriptions(::toSubscription).executeAsList()
    }

    suspend fun deleteSubscriptionByPushTopic(pushTopic: String) = withContext(Dispatchers.IO) {
        subscriptionQueries.deleteByTopicByPushTopic(pushTopic)
    }

    suspend fun deleteSubscriptionByRequestId(requestId: Long) = withContext(Dispatchers.IO) {
        subscriptionQueries.deleteByTopicByRequestId(requestId)
    }

    private fun toSubscription(
        request_id: Long,
        subscribe_topic: String,
        dapp_did_public_key: String,
        self_public_key: String,
        response_topic: String,
        account: String,
        dapp_uri: String,
        did_jwt: String,
        map_of_scope: Map<String, Pair<String, Boolean>>,
        expiry: Long,
        dapp_generated_public_key: String?,
        push_topic: String?,
    ): EngineDO.PushSubscribe {
        return if (!dapp_generated_public_key.isNullOrBlank() && !push_topic.isNullOrBlank()) {
            EngineDO.PushSubscribe.Responded(
                requestId = request_id,
                subscribeTopic = Topic(subscribe_topic),
                dappDidPublicKey = PublicKey(dapp_did_public_key),
                selfPublicKey = PublicKey(self_public_key),
                responseTopic = Topic(response_topic),
                account = AccountId(account),
                mapOfScope = map_of_scope.map { entry -> entry.key to EngineDO.PushScope.Cached(entry.key, entry.value.first, entry.value.second) }.toMap(),
                expiry = Expiry(expiry),
                dappGeneratedPublicKey = PublicKey(dapp_generated_public_key),
                pushTopic = Topic(push_topic)
            )
        } else {
            EngineDO.PushSubscribe.Requested(
                requestId = request_id,
                subscribeTopic = Topic(subscribe_topic),
                dappDidPublicKey = PublicKey(dapp_did_public_key),
                selfPublicKey = PublicKey(self_public_key),
                responseTopic = Topic(response_topic),
                account = AccountId(account),
                mapOfScope = map_of_scope.map { entry -> entry.key to EngineDO.PushScope.Cached(entry.key, entry.value.first, entry.value.second) }.toMap(),
                expiry = Expiry(expiry),
            )
        }
    }
}