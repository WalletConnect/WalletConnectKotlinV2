package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.type.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.utils.JsonRpcMethod

internal sealed class PostSettlementSessionVO : ClientSyncJsonRpc {
    abstract override val id: Long
    abstract val method: String
    abstract val jsonrpc: String
    abstract val params: SessionParamsVO

    @JsonClass(generateAdapter = true)
    data class SessionPayload(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PAYLOAD,
        @Json(name = "params")
        override val params: SessionParamsVO.SessionPayloadParams
    ) : PostSettlementSessionVO()

    @JsonClass(generateAdapter = true)
    data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_DELETE,
        @Json(name = "params")
        override val params: SessionParamsVO.DeleteParams
    ) : PostSettlementSessionVO()

    @JsonClass(generateAdapter = true)
    data class SessionUpdate(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE,
        @Json(name = "params")
        override val params: SessionParamsVO.UpdateParams
    ) : PostSettlementSessionVO()

    @JsonClass(generateAdapter = true)
    data class SessionUpgrade(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPGRADE,
        @Json(name = "params")
        override val params: SessionParamsVO.SessionPermissionsParams
    ) : PostSettlementSessionVO()

    @JsonClass(generateAdapter = true)
    data class SessionPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PING,
        @Json(name = "params")
        override val params: SessionParamsVO.PingParams
    ) : PostSettlementSessionVO()

    @JsonClass(generateAdapter = true)
    data class SessionNotification(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_NOTIFICATION,
        @Json(name = "params")
        override val params: SessionParamsVO.NotificationParams
    ) : PostSettlementSessionVO()
}