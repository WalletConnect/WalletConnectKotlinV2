package com.walletconnect.push.common.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.scope
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.SubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionStorageRepository(private val subscriptionQueries: SubscriptionsQueries) {

    suspend fun insertSubscription(
        requestId: Long,
        responseTopic: String,
        peerPublicKeyAsHex: String? = null,
        subscriptionTopic: String? = null,
        account: String,
        relayProtocol: String?,
        relayData: String?,
        name: String,
        description: String,
        url: String,
        icons: List<String>,
        native: String?,
        didJwt: String,
        mapOfScope: Map<String, Pair<String, Boolean>>,
        expiry: Long
    ) = withContext(Dispatchers.IO) {
        subscriptionQueries.insertSubscription(
            request_id = requestId,
            pairing_topic = responseTopic,
            peer_public_key = peerPublicKeyAsHex,
            topic = subscriptionTopic,
            account = account,
            relay_protocol = relayProtocol ?: "irn",
            relay_data = relayData,
            metadata_name = name,
            metadata_description = description,
            metadata_url = url,
            metadata_icons = icons,
            metadata_native = native
        )
    }

    suspend fun updateSubscriptionToResponded(responseTopic: String, topic: String, dappPublicKey: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        val (relay, data) = RelayProtocolOptions().run { protocol to data }
        subscriptionQueries.updateSubscriptionToResponded(topic, relay, data, metadata.name, metadata.description, metadata.url, metadata.icons, metadata.redirect?.native, requestId)
    }

    suspend fun updateSubscriptionToRespondedByApproval(responseTopic: String, topic: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        val (relay, data) = RelayProtocolOptions().run { protocol to data }
        subscriptionQueries.updateSubscriptionToResponded(topic, relay, data, metadata.name, metadata.description, metadata.url, metadata.icons, metadata.redirect?.native, requestId)
    }

    suspend fun updateSubscriptionScopeAndJwt(subscriptionTopic: String, updateScope: Map<String, Pair<String, Boolean>>, updateJwt: String, newExpiry: Long) = withContext(Dispatchers.IO) {
        subscriptionQueries.updateSubscriptionScopeAndJwt(scope, didJwt, requestId)
    }

    suspend fun getAllSubscriptions(): List<EngineDO.PushSubscription> = withContext(Dispatchers.IO) {
        subscriptionQueries.getAllSubscriptions(::toSubscription).executeAsList()
    }

    suspend fun deleteSubscription(topic: String) = withContext(Dispatchers.IO) {
        subscriptionQueries.deleteByTopic(topic)
    }

    suspend fun getAccountByTopic(topic: String): String = withContext(Dispatchers.IO) {
        subscriptionQueries.getSubscriptionByTopic(topic).executeAsOne().account
    }

    suspend fun getSubscriptionsByRequestId(requestId: Long): EngineDO.PushSubscription = withContext(Dispatchers.IO) {
        subscriptionQueries.getSubscriptionByRequestId(requestId, ::toSubscription).executeAsOne()
    }

    private fun toSubscription(
        request_id: Long,
        pairingTopic: String,
        peerPublicKey: String,
        topic: String?,
        account: String,
        relay_protocol: String,
        relay_data: String?,
        metadata_name: String,
        metadata_description: String,
        metadata_url: String,
        metadata_icons: List<String>,
        metadata_native: String?,
    ): EngineDO.PushSubscription {
        val metadata = AppMetaData(metadata_name, metadata_description, metadata_url, metadata_icons, Redirect(metadata_native))
        val relayProtocolOptions = RelayProtocolOptions(relay_protocol, relay_data)

        return EngineDO.PushSubscription(request_id, pairingTopic, peerPublicKey, topic, AccountId(account), relayProtocolOptions, metadata, "")
    }
}