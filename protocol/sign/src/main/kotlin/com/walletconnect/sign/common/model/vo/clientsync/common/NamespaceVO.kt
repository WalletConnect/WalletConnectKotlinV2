@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


internal sealed class NamespaceVO {
    abstract val chains: List<String>?
    abstract val methods: List<String>
    abstract val events: List<String>

    //    Proposal Namespaces -> Required or Optional Namespaces
    @JsonClass(generateAdapter = true)
    internal data class Proposal(
        @Json(name = "methods")
        override val methods: List<String>,
        @Json(name = "chains")
        override val chains: List<String>? = null,
        @Json(name = "events")
        override val events: List<String>
    ) : NamespaceVO()

    @JsonClass(generateAdapter = true)
    internal data class Session(
        @Json(name = "chains")
        override val chains: List<String>? = null,
        @Json(name = "accounts")
        val accounts: List<String>,
        @Json(name = "methods")
        override val methods: List<String>,
        @Json(name = "events")
        override val events: List<String>
    ) : NamespaceVO()
}