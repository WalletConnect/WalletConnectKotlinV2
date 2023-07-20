package com.walletconnect.android.verify.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterAttestationBody(val attestationId: String, val origin: String)