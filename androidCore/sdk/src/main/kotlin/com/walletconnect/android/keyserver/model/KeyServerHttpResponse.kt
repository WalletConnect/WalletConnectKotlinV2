package com.walletconnect.android.keyserver.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class KeyServerHttpResponse<T> {
    abstract val status: String
    abstract val error: Error?
    abstract val value: T?

    companion object {
        const val SUCCESS_STATUS = "SUCCESS"
    }

    @JsonClass(generateAdapter = true)
    data class RegisterInvite(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: String?,
    ) : KeyServerHttpResponse<String>()

    @JsonClass(generateAdapter = true)
    data class UnregisterInvite(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: String?,
    ) : KeyServerHttpResponse<String>()

    @JsonClass(generateAdapter = true)
    data class ResolveInvite(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: KeyServerResponse.ResolveInvite,
    ) : KeyServerHttpResponse<KeyServerResponse.ResolveInvite>()

    @JsonClass(generateAdapter = true)
    data class RegisterIdentity(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: String?,
    ) : KeyServerHttpResponse<String>()

    @JsonClass(generateAdapter = true)
    data class UnregisterIdentity(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: String?,
    ) : KeyServerHttpResponse<String>()

    @JsonClass(generateAdapter = true)
    data class ResolveIdentity(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: KeyServerResponse.ResolveIdentity,
    ) : KeyServerHttpResponse<KeyServerResponse.ResolveIdentity>()

    @JsonClass(generateAdapter = true)
    data class Error(
        @Json(name = "message")
        val message: String,
        @Json(name = "name")
        val name: String,
    )
}