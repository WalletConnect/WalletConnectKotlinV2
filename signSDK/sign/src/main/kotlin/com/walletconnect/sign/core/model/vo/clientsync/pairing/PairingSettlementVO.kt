package com.walletconnect.sign.core.model.vo.clientsync.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.sign.core.model.type.SettlementSequence
import com.walletconnect.sign.core.model.utils.JsonRpcMethod
import com.walletconnect.sign.core.model.vo.clientsync.pairing.params.PairingParamsVO

internal sealed class PairingSettlementVO : SettlementSequence<PairingParamsVO> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: PairingParamsVO

    @JsonClass(generateAdapter = true)
    internal data class SessionPropose(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PROPOSE,
        @Json(name = "params")
        override val params: PairingParamsVO.SessionProposeParams,
    ) : PairingSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class PairingDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_DELETE,
        @Json(name = "params")
        override val params: PairingParamsVO.DeleteParams,
    ) : PairingSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class PairingPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_PING,
        @Json(name = "params")
        override val params: PairingParamsVO.PingParams,
    ) : PairingSettlementVO()
}