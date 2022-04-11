package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.SettlementSequence
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO

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
    internal data class SessionUpdateEvents(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE_EVENTS,
        @Json(name = "params")
        override val params: SessionParamsVO.UpdateEventsParams,
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionUpdateAccounts(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE_ACCOUNTS,
        @Json(name = "params")
        override val params: SessionParamsVO.UpdateAccountsParams,
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionUpdateMethods(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE_METHODS,
        @Json(name = "params")
        override val params: SessionParamsVO.UpdateMethodsParams,
    ) : SessionSettlementVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionUpdateExpiry(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE_EXPIRY,
        @Json(name = "params")
        override val params: SessionParamsVO.UpdateExpiryParams,
    ) : SessionSettlementVO()
}