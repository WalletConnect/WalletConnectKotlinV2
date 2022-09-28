@file:JvmSynthetic

package com.walletconnect.android.common.model.metadata

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RelayProtocolOptions(
    val protocol: String = "irn",
    val data: String? = null
)