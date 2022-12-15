package com.walletconnect.foundation.common.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class SubscriptionId(val id: String)