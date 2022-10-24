@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.session

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.JsonRpcClientSync
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParamsVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod

internal sealed class SignRpcVO : JsonRpcClientSync<SignParamsVO> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: SignParamsVO

    @JsonClass(generateAdapter = true)
    internal data class SessionPropose(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PROPOSE,
        @Json(name = "params")
        override val params: SignParamsVO.SessionProposeParams,
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionSettle(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_SETTLE,
        @Json(name = "params")
        override val params: SignParamsVO.SessionSettleParams
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionRequest(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_REQUEST,
        @Json(name = "params")
        override val params: SignParamsVO.SessionRequestParams
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_DELETE,
        @Json(name = "params")
        override val params: SignParamsVO.DeleteParams
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PING,
        @Json(name = "params")
        override val params: SignParamsVO.PingParams,
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionEvent(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_EVENT,
        @Json(name = "params")
        override val params: SignParamsVO.EventParams,
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionUpdate(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_UPDATE,
        @Json(name = "params")
        override val params: SignParamsVO.UpdateNamespacesParams,
    ) : SignRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionExtend(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_EXTEND,
        @Json(name = "params")
        override val params: SignParamsVO.ExtendParams,
    ) : SignRpcVO()
}