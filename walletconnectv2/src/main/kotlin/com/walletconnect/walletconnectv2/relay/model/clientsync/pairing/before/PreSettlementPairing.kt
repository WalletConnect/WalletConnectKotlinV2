package com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.Pairing
import com.walletconnect.walletconnectv2.relay.model.JsonRpcMethod

sealed class PreSettlementPairing : ClientSyncJsonRpc {
    abstract override val id: Long
    abstract val method: String
    abstract val jsonrpc: String
    abstract val params: Pairing

    @JsonClass(generateAdapter = true)
    data class Approve(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_APPROVE,
        @Json(name = "params")
        override val params: Pairing.Success
    ) : PreSettlementPairing()

    data class Reject(
        override val id: Long,
        override val jsonrpc: String = "2.0",
        override val method: String = JsonRpcMethod.WC_PAIRING_REJECT,
        override val params: Pairing.Failure
    ) : PreSettlementPairing()
}

