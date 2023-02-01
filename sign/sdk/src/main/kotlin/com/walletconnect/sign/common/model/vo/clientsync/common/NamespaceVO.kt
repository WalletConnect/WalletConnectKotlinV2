@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

internal sealed class NamespaceVO {

    //todo: change name to required namespaces and optional namespaces
    @JsonClass(generateAdapter = true)
    internal data class Proposal(
        @Json(name = "chains")
        val chains: List<String>,
        @Json(name = "methods")
        val methods: List<String>,
        @Json(name = "events")
        val events: List<String>
    ) : NamespaceVO()

    @JsonClass(generateAdapter = true)
    internal data class Session(
//        @Json(name = "chains")
//        val chains: List<String>,
        @Json(name = "accounts")
        val accounts: List<String>,
        @Json(name = "methods")
        val methods: List<String>,
        @Json(name = "events")
        val events: List<String>
    ) : NamespaceVO()
}