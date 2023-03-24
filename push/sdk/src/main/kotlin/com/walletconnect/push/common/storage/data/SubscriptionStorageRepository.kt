package com.walletconnect.push.common.storage.data

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.SubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionStorageRepository(private val subscriptionQueries: SubscriptionsQueries) {

    fun insertSubscriptionProposal(requestId: Long, pairingTopic: String, peerPublicKey: String, account: String, name: String, description: String, url: String, icons: List<String>, native: String?) {
        subscriptionQueries.insertSubscriptionRequest(
            requestId,
            pairingTopic,
            peerPublicKey,
            account,
            name,
            description,
            url,
            icons,
            native
        )
    }

    fun insertRespondedSubscription(pairingTopic: String, respondedSubscription: EngineDO.PushSubscription.Responded) {
        subscriptionQueries.insertSubscriptionResponse(
            respondedSubscription.requestId,
            pairingTopic,
            respondedSubscription.peerPublicKey,
            respondedSubscription.topic,
            respondedSubscription.account,
            respondedSubscription.relay.protocol,
            respondedSubscription.relay.data,
            respondedSubscription.metadata.name,
            respondedSubscription.metadata.description,
            respondedSubscription.metadata.url,
            respondedSubscription.metadata.icons,
            respondedSubscription.metadata.redirect?.native
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

    suspend fun getAccountByTopic(topic: String): String? {
        return withContext(Dispatchers.IO) {
            subscriptionQueries.getSubscriptionByTopic(topic).executeAsOne().account
        }
    }

    fun getSubscriptionsByRequestId(requestId: Long): EngineDO.PushSubscription.Requested? {
        return (subscriptionQueries.getSubscriptionByRequestId(requestId, ::toSubscription).executeAsOne() as? EngineDO.PushSubscription.Requested)
    }

    private fun toSubscription(
        id: Long,
        request_id: Long,
        pairingTopic: String,
        peerPublicKey: String,
        topic: String?,
        account: String?,
        relay_protocol: String?,
        relay_data: String?,
        metadata_name: String,
        metadata_description: String,
        metadata_url: String,
        metadata_icons: List<String>,
        metadata_native: String?,
    ): EngineDO.PushSubscription {
        val metadata = AppMetaData(metadata_name, metadata_description, metadata_url, metadata_icons, Redirect(metadata_native))

        return if (topic == null || account == null) {
            EngineDO.PushSubscription.Requested(request_id, pairingTopic, peerPublicKey, metadata)
        } else {
            EngineDO.PushSubscription.Responded(request_id, pairingTopic, peerPublicKey, topic, account, RelayProtocolOptions(relay_protocol ?: "irn", relay_data), metadata)
        }
    }
}