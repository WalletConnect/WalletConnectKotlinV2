package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.after

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.SettlementSequence
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingParamsVO

internal sealed class PostSettlementPairingVO : SettlementSequence<PairingParamsVO> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: PairingParamsVO

    @JsonClass(generateAdapter = true)
    internal data class PairingPayload(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_PAYLOAD,
        @Json(name = "params")
        override val params: PairingParamsVO.PayloadParams
    ) : PostSettlementPairingVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_DELETE,
        @Json(name = "params")
        override val params: PairingParamsVO.DeleteParams
    ) : PostSettlementPairingVO()

    @JsonClass(generateAdapter = true)
    internal data class PairingUpdate(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_UPDATE,
        @Json(name = "params")
        override val params: PairingParamsVO.UpdateParams
    ) : PostSettlementPairingVO()

    @JsonClass(generateAdapter = true)
    internal data class PairingPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_PING,
        @Json(name = "params")
        override val params: PairingParamsVO.PingParams
    ) : PostSettlementPairingVO()

    @JsonClass(generateAdapter = true)
    internal data class PairingNotification(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PAIRING_NOTIFICATION,
        @Json(name = "params")
        override val params: PairingParamsVO.NotificationParams
    ) : PostSettlementPairingVO()
}