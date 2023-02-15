@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.Expiry

@JsonClass(generateAdapter = false)
internal data class SessionRequestVO(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: String,
    @Json(name = "expiry")
    val expiry: Expiry? = null
)