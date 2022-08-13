package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json

internal data class RedirectVO(
    @Json(name = "native")
    val native: String? = null,
    @Json(name = "universal")
    val universal: String? = null
)