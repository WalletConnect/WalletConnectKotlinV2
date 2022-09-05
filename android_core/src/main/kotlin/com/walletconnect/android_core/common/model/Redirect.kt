@file:JvmSynthetic

package com.walletconnect.android_core.common.model

import com.squareup.moshi.Json

data class Redirect(
    @Json(name = "native")
    val native: String? = null,
    @Json(name = "universal")
    val universal: String? = null
)