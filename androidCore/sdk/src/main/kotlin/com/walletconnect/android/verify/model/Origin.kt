package com.walletconnect.android.verify.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Origin(val attestationId: String, val origin: String)