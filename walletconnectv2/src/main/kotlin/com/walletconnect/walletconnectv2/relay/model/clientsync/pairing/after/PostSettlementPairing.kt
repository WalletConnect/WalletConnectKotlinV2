package com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.after

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.Pairing
import com.walletconnect.walletconnectv2.relay.model.JsonRpcMethod

internal sealed class PostSettlementPairing : ClientSyncJsonRpc {
    abstract override val id: Long
    abstract val method: String
    abstract val jsonrpc: String
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
    ) : PostSettlementPairing()

    @JsonClass(generateAdapter = true)
    data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_DELETE,
        @Json(name = "params")
        override val params: Pairing.DeleteParams
    ) : PostSettlementPairing()

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

    @JsonClass(generateAdapter = true)
    data class PairingNotification(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_NOTIFICATION,
        @Json(name = "params")
        override val params: Pairing.NotificationParams
    ) : PostSettlementPairing()
}