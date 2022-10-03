@file:JvmSynthetic

package com.walletconnect.android.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.common.model.MetaData

@JsonClass(generateAdapter = true)
internal data class SessionProposer(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: MetaData,
)