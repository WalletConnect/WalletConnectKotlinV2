package com.walletconnect.push.common.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.SubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionStorageRepository(private val subscriptionQueries: SubscriptionsQueries) {

    suspend fun insertSubscription(
        requestId: Long,
        keyAgreementTopic: String,
        responseTopic: String,
        peerPublicKeyAsHex: String? = null,
        subscriptionTopic: String? = null,
        account: String,
        relayProtocol: String?,
        relayData: String? = null,
        name: String,
        description: String,
        url: String,
        icons: List<String>,
        native: String?,
        didJwt: String,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        expiry: Long,
    ) = withContext(Dispatchers.IO) {
        subscriptionQueries.insertSubscription(
            request_id = requestId,
            key_agreement_topic = keyAgreementTopic,
            response_topic = responseTopic,
            dapp_public_key = peerPublicKeyAsHex,
            topic = subscriptionTopic,
            account = account,
            relay_protocol = relayProtocol ?: "irn",
            relay_data = relayData,
            metadata_name = name,
            metadata_description = description,
            metadata_url = url,
            metadata_icons = icons,
            metadata_native = native,
            did_jwt = didJwt,
            map_of_scope = mapOfScope,
            expiry = expiry
        )
    }

    suspend fun updateSubscriptionToResponded(responseTopic: String, topic: String, dappPublicKey: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        subscriptionQueries.updateSubscriptionWithDappPublicKeyToResponded(topic, dappPublicKey, newExpiry, responseTopic)
    }

    suspend fun updateSubscriptionToRespondedByApproval(responseTopic: String, topic: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        val (relay, data) = RelayProtocolOptions().run { protocol to data }
        subscriptionQueries.updateSubscriptionToResponded(topic, newExpiry, responseTopic)
    }

    suspend fun updateSubscriptionScopeAndJwt(subscriptionTopic: String, updateScope: Map<String, Pair<String, Boolean>>, updateJwt: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        subscriptionQueries.updateSubscriptionScopeAndJwt(updateScope, updateJwt, newExpiry, subscriptionTopic)
    }

    suspend fun getAllSubscriptions(): List<EngineDO.PushSubscription> = withContext(Dispatchers.IO) {
        subscriptionQueries.getAllSubscriptions(::toSubscription).executeAsList()
    }

    suspend fun deleteSubscription(topic: String) = withContext(Dispatchers.IO) {
        subscriptionQueries.deleteByTopic(topic)
    }

    suspend fun getAccountByTopic(topic: String): String? = withContext(Dispatchers.IO) {
        subscriptionQueries.getSubscriptionByTopic(topic).executeAsOneOrNull()?.account
    }

    suspend fun getSubscriptionsByRequestId(requestId: Long): EngineDO.PushSubscription = withContext(Dispatchers.IO) {
        subscriptionQueries.getSubscriptionByRequestId(requestId, ::toSubscription).executeAsOne()
    }

    private fun toSubscription(
        request_id: Long,
        keyAgreementTopic: String,
        responseTopic: String,
        dappPublicKey: String?,
        topic: String?,
        account: String,
        relay_protocol: String,
        relay_data: String?,
        metadata_name: String,
        metadata_description: String,
        metadata_url: String,
        metadata_icons: List<String>,
        metadata_native: String?,
        did_jwt: String,
        map_of_scope: Map<String, Pair<String, Boolean>>,
        expiry: Long,
    ): EngineDO.PushSubscription {
        val metadata = AppMetaData(metadata_name, metadata_description, metadata_url, metadata_icons, Redirect(metadata_native))
        val relayProtocolOptions = RelayProtocolOptions(relay_protocol, relay_data)

        return EngineDO.PushSubscription(
            request_id,
            Topic(keyAgreementTopic),
            Topic(responseTopic),
            dappPublicKey?.let { PublicKey(it) },
            topic?.let { Topic(it) },
            AccountId(account),
            relayProtocolOptions,
            metadata,
            did_jwt,
            map_of_scope,
            Expiry(expiry)
        )
    }
}