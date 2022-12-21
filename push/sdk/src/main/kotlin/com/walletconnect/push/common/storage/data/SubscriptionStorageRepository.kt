package com.walletconnect.push.common.storage.data

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.SubscriptionsQueries

class SubscriptionStorageRepository(private val subscriptionQueries: SubscriptionsQueries) {

    fun insertSubscriptionRequest(requestId: Long, peerPublicKey: String) {
        subscriptionQueries.insertSubscriptionRequest(requestId, peerPublicKey)
    }

    fun insertRespondedSubscription(respondedSubscription: EngineDO.PushSubscription.Responded) {
        subscriptionQueries.insertSubscriptionResponse(
            respondedSubscription.requestId,
            respondedSubscription.peerPublicKey,
            respondedSubscription.topic,
            respondedSubscription.relay.protocol,
            respondedSubscription.relay.data,
            respondedSubscription.metadata?.name,
            respondedSubscription.metadata?.description,
            respondedSubscription.metadata?.url,
            respondedSubscription.metadata?.icons,
            respondedSubscription.metadata?.redirect?.native)
    }

    fun updateSubscriptionToResponded(requestId: Long, topic: String, metadata: AppMetaData) {
        val (relay, data) = RelayProtocolOptions().run { protocol to data }
        subscriptionQueries.updateSubscriptionToResponded(topic, relay, data, metadata.name, metadata.description, metadata.url, metadata.icons, metadata.redirect?.native, requestId)
    }

    fun getAllSubscriptions(): List<EngineDO.PushSubscription> =
        subscriptionQueries.getAllSubscriptions(::toSubscription).executeAsList()

    fun getSubscriptionByTopic(topic: String) = subscriptionQueries.getSubscriptionByTopic(topic, ::toSubscription).executeAsOne()

    fun getPeerPublicKeyByRequestId(requestId: Long) = subscriptionQueries.getPeerPublicKeyByRequestId(requestId).executeAsOne()

    fun delete(topic: String) {
        subscriptionQueries.deleteByTopic(topic)
    }


    private fun toSubscription(
        id: Long,
        request_id: Long,
        peerPublicKey: String,
        topic: String?,
        relay_protocol: String?,
        relay_data: String?,
        metadata_name: String?,
        metadata_description: String?,
        metadata_url: String?,
        metadata_icons: List<String>?,
        metadata_native: String?,
    ): EngineDO.PushSubscription {
        return if (topic == null) {
            EngineDO.PushSubscription.Requested(request_id, peerPublicKey)
        } else {
            val metadata = if (metadata_name != null && metadata_description != null && metadata_url != null && metadata_icons != null && metadata_native != null) {
                AppMetaData(metadata_name, metadata_description, metadata_url, metadata_icons, Redirect(metadata_native))
            } else {
                null
            }

            EngineDO.PushSubscription.Responded(request_id, peerPublicKey, topic, RelayProtocolOptions(relay_protocol ?: "irn", relay_data), metadata)
        }
    }
}