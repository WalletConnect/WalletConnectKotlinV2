package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionProposedPermissionsVO(
    @Json(name = "blockchain")
    val blockchain: Blockchain,
    @Json(name = "jsonrpc")
    val jsonRpc: JsonRpc,
    @Json(name = "notifications")
    val notifications: Notifications = Notifications(listOf())
) {

    @JsonClass(generateAdapter = true)
    data class Blockchain(
        @Json(name = "chains")
        val chains: List<String>
    )

    @JsonClass(generateAdapter = true)
    data class JsonRpc(
        @Json(name = "methods")
        val methods: List<String>
    )

    @JsonClass(generateAdapter = true)
    data class Notifications(
        @Json(name = "types")
        val types: List<String>
    )
}
