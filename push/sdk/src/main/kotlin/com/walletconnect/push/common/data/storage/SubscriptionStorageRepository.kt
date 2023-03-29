package com.walletconnect.push.common.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.SubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionStorageRepository(private val subscriptionQueries: SubscriptionsQueries) {

    fun insertSubscription(requestId: Long, pairingTopic: String, peerPublicKeyAsHex: String, subscriptionTopic: String? = null, account: String, relayProtocol: String?, relayData: String?, name: String, description: String, url: String, icons: List<String>, native: String?) {
        subscriptionQueries.insertSubscription(
            request_id = requestId,
            pairing_topic = pairingTopic,
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

    fun updateSubscriptionToResponded(requestId: Long, topic: String, metadata: AppMetaData) {
        val (relay, data) = RelayProtocolOptions().run { protocol to data }
        subscriptionQueries.updateSubscriptionToResponded(topic, relay, data, metadata.name, metadata.description, metadata.url, metadata.icons, metadata.redirect?.native, requestId)
    }

    fun getAllSubscriptions(): List<EngineDO.PushSubscription> =
        subscriptionQueries.getAllSubscriptions(::toSubscription).executeAsList()

    fun deleteSubscription(topic: String) {
        subscriptionQueries.deleteByTopic(topic)
    }

    suspend fun getAccountByTopic(topic: String): String {
        return withContext(Dispatchers.IO) {
            subscriptionQueries.getSubscriptionByTopic(topic).executeAsOne().account
        }
    }

    fun getSubscriptionsByRequestId(requestId: Long): EngineDO.PushSubscription {
        return subscriptionQueries.getSubscriptionByRequestId(requestId, ::toSubscription).executeAsOne()
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

        return EngineDO.PushSubscription(request_id, pairingTopic, peerPublicKey, topic, AccountId(account), relayProtocolOptions, metadata)
    }
}