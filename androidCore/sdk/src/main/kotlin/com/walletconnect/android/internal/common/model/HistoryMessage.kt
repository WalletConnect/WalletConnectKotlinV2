package com.walletconnect.android.internal.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.util.generateId

@JsonClass(generateAdapter = true)
data class HistoryMessage(
    val topic: String,
    val message: String,
    val method: String,
    @Json(name = "message_id")
    val messageId: String,
    @Json(name = "client_id")
    val clientId: String,
) {
    fun toRelay() = Relay.Model.Call.Subscription.Request(
        generateId(), //todo: this is very wrong! Duplicates may occur. Needs refactor to message_id instead of rpc_id
        params = Relay.Model.Call.Subscription.Request.Params(messageId, Relay.Model.Call.Subscription.Request.Params.SubscriptionData(topic, message))
    )
}