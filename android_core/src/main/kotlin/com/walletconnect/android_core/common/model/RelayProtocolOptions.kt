@file:JvmSynthetic

package com.walletconnect.android_core.common.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RelayProtocolOptions(
    val protocol: String = "irn",
    val data: String? = null
)