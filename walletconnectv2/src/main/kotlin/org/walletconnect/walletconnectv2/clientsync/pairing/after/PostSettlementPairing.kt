package org.walletconnect.walletconnectv2.clientsync.pairing.after

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod

sealed class PostSettlementPairing {
    abstract val id: Long
    abstract val jsonrpc: String
    abstract val method: String
    abstract val params: Pairing

    @JsonClass(generateAdapter = true)
    data class PairingPayload(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_PAYLOAD,
        @Json(name = "params")
        override val params: Pairing.PayloadParams
    ) : PostSettlementPairing() {
        val payloadParams = params.request.params
    }

    @JsonClass(generateAdapter = true)
    data class PairingUpdate(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_UPDATE,
        @Json(name = "params")
        override val params: Pairing.UpdateParams
    ) : PostSettlementPairing()

    @JsonClass(generateAdapter = true)
    data class PairingPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_PING,
        @Json(name = "params")
        override val params: Pairing.PingParams
    ) : PostSettlementPairing()
}