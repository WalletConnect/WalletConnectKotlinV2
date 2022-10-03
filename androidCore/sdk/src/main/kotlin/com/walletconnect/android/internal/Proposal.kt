package com.walletconnect.android.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
) {

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