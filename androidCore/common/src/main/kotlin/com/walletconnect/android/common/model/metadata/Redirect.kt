@file:JvmSynthetic

package com.walletconnect.android.common.model.metadata

import com.squareup.moshi.Json

data class Redirect(
    @Json(name = "native")
    val native: String? = null,
    @Json(name = "universal")
    val universal: String? = null
)