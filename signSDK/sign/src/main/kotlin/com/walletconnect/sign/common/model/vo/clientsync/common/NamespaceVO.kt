@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

internal sealed class NamespaceVO {

    @JsonClass(generateAdapter = true)
    internal data class Proposal(
        @Json(name = "chains")
        val chains: List<String>,
        @Json(name = "methods")
        val methods: List<String>,
        @Json(name = "events")
        val events: List<String>,
        @Json(name = "extension")
        val extensions: List<Extension>?
    ): NamespaceVO() {

        @JsonClass(generateAdapter = true)
        internal data class Extension(
            @Json(name = "chains")
            val chains: List<String>,
            @Json(name = "methods")
            val methods: List<String>,
            @Json(name = "events")
            val events: List<String>
        )
    }

    @JsonClass(generateAdapter = true)
    internal data class Session(
        @Json(name = "accounts")
        val accounts: List<String>,
        @Json(name = "methods")
        val methods: List<String>,
        @Json(name = "events")
        val events: List<String>,
        @Json(name = "extension")
        val extensions: List<Extension>?
    ): NamespaceVO() {

        @JsonClass(generateAdapter = true)
        internal data class Extension(
            @Json(name = "accounts")
            val accounts: List<String>,
            @Json(name = "methods")
            val methods: List<String>,
            @Json(name = "events")
            val events: List<String>
        )
    }
}