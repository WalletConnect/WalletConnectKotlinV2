package com.walletconnect.notify.common.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ServerSubscription(
    val appDomain: String,
    val symKey: String,
    val account: String,
    val scope: List<String>,
    val expiry: Long,
)
