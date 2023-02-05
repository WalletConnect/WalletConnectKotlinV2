@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

internal sealed class KeyServerHttpResponse<T : KeyServerResponse> {
    abstract val status: String
    abstract val error: Error?
    abstract val value: T?

    @JsonClass(generateAdapter = true)
    internal data class ResolveInvite(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: KeyServerResponse.ResolveInvite,
    ) : KeyServerHttpResponse<KeyServerResponse.ResolveInvite>()

    @JsonClass(generateAdapter = true)
    internal data class ResolveIdentity(
        @Json(name = "status")
        override val status: String,
        @Json(name = "error")
        override val error: Error?,
        @Json(name = "value")
        override val value: KeyServerResponse.ResolveIdentity,
    ) : KeyServerHttpResponse<KeyServerResponse.ResolveIdentity>()

    @JsonClass(generateAdapter = true)
    internal data class Error(
        @Json(name = "message")
        val message: String,
        @Json(name = "name")
        val name: String,
    )
}