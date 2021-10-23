package org.walletconnect.walletconnectv2.clientcomm.pairing


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientcomm.session.Session

@JsonClass(generateAdapter = true)
data class PairingPayload(
    @Json(name = "id")
    val id: Long,
    @Json(name = "jsonrpc")
    val jsonrpc: String,
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Params
) {
    @JsonClass(generateAdapter = true)
    data class Params(
        @Json(name = "request")
        val request: Request
    ) {
        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "method")
            val method: String,
            @Json(name = "params")
            val params: Session.Proposal
        )
    }
}