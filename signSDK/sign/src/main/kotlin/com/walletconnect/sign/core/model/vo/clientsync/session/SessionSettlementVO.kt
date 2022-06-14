package com.walletconnect.sign.core.model.vo.clientsync.session

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.sign.core.model.type.SettlementSequence
import com.walletconnect.sign.core.model.utils.JsonRpcMethod
import com.walletconnect.sign.core.model.vo.clientsync.session.params.SessionParamsVO

internal sealed class SessionSettlementVO : SettlementSequence<SessionParamsVO> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: SessionParamsVO

    @JsonClass(generateAdapter = true)
    internal data class SessionSettle(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_SETTLE,
        @Json(name = "params")
        override val params: SessionParamsVO.SessionSettleParams
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionRequest(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_REQUEST,
        @Json(name = "params")
        override val params: SessionParamsVO.SessionRequestParams
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_DELETE,
        @Json(name = "params")
        override val params: SessionParamsVO.DeleteParams
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PING,
        @Json(name = "params")
        override val params: SessionParamsVO.PingParams,
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionEvent(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_EVENT,
        @Json(name = "params")
        override val params: SessionParamsVO.EventParams,
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionUpdateNamespaces(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE,
        @Json(name = "params")
        override val params: SessionParamsVO.UpdateNamespacesParams,
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionExtend(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_EXTEND,
        @Json(name = "params")
        override val params: SessionParamsVO.ExtendParams,
    ) : SessionSettlementVO()
}