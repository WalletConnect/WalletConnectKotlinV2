package org.walletconnect.walletconnectv2.clientcomm.session.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionProposedPermissions(
    @Json(name = "blockchain")
    val blockchain: Blockchain,
    @Json(name = "jsonrpc")
    val jsonRpc: JsonRpc,
    @Json(name = "notifications")
    val notifications: Notifications
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
