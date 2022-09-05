@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionEventVO(
    @Json(name = "name")
    val name: String,
    @Json(name = "data")
    val data: Any,
)