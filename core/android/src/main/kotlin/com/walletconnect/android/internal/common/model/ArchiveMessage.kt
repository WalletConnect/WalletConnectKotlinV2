package com.walletconnect.android.internal.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.foundation.network.model.Relay

@JsonClass(generateAdapter = true)
data class ArchiveMessage(
    val topic: String,
    val message: String,
    val method: String,
    @Json(name = "message_id")
    val messageId: String,
    @Json(name = "client_id")
    val clientId: String,
) {
    fun toRelay() = Relay.Model.Call.Subscription.Request(
        id = 0L, // id is only used for ack the relay, any value is meaningless
        params = Relay.Model.Call.Subscription.Request.Params(
            subscriptionId = messageId,
            subscriptionData = Relay.Model.Call.Subscription.Request.Params.SubscriptionData(
                topic = topic,
                message = message,
                publishedAt = 0L, // publishedAt is only used for ack the relay, any value is meaningless
                tag = 0 // tag is only used for ack the relay, any value is meaningless
            )
        )
    )
}