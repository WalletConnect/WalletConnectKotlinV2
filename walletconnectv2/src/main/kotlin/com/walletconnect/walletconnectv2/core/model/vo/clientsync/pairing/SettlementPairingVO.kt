package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.SettlementSequence
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO

internal sealed class SettlementPairingVO : SettlementSequence<PairingParamsVO> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: PairingParamsVO

    //todo: add wc_pairingExtend

    //todo: check session propose model
    @JsonClass(generateAdapter = true)
    internal data class PairingPayload(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PROPOSE,
        @Json(name = "params")
        override val params: PairingParamsVO.PayloadParams,
    ) : SettlementPairingVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_DELETE,
        @Json(name = "params")
        override val params: PairingParamsVO.DeleteParams,
    ) : SettlementPairingVO()

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
    ) : SettlementPairingVO()
}