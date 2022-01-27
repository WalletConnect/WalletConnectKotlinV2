package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.before

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingParamsVO

internal sealed class PreSettlementPairingVO : ClientSyncJsonRpc {
    abstract override val id: Long
    abstract val method: String
    abstract val jsonrpc: String
    abstract val params: PairingParamsVO

    @JsonClass(generateAdapter = true)
    internal data class Approve(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_APPROVE,
        @Json(name = "params")
        override val params: PairingParamsVO.ApproveParams
    ) : PreSettlementPairingVO()

    internal data class Reject(
        override val id: Long,
        override val jsonrpc: String = "2.0",
        override val method: String = JsonRpcMethod.WC_PAIRING_REJECT,
        override val params: PairingParamsVO.RejectParams
    ) : PreSettlementPairingVO()
}

