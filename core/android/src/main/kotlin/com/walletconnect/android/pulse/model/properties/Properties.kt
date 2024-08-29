package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Properties(
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "method")
    val method: String? = null,
    @Json(name = "connected")
    val connected: Boolean? = null,
    @Json(name = "network")
    val network: String? = null,
    @Json(name = "platform")
    val platform: String? = null,
    @Json(name = "trace")
    val trace: List<String>? = null,
    @Json(name = "topic")
    val topic: String? = null,
    @Json(name = "correlation_id")
    val correlationId: Long? = null,
    @Json(name = "client_id")
    val clientId: String? = null,
    @Json(name = "direction")
    val direction: String? = null,
    @Json(name = "user_agent")
    val userAgent: String? = null,
)