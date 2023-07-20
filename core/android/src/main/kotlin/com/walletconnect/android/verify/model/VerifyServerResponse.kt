package com.walletconnect.android.verify.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class VerifyServerResponse<T> {
    abstract val error: Error?
    abstract val value: T?

    @JsonClass(generateAdapter = true)
    data class RegisterAttestation(
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: String?,
    ) : VerifyServerResponse<String>()

    @JsonClass(generateAdapter = true)
    data class ResolveAttestation(
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: Origin?,
    ) : VerifyServerResponse<Origin>()

    @JsonClass(generateAdapter = true)
    data class Error(
        @Json(name = "message")
        val message: String,
        @Json(name = "name")
        val name: String,
    )
}