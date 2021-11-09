package org.walletconnect.walletconnectv2.clientsync.session.after

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod

sealed class PostSettlementSession {
    abstract val id: Long
    abstract val jsonrpc: String
    abstract val method: String
    abstract val params: Session

    @JsonClass(generateAdapter = true)
    data class SessionPayload(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PAYLOAD,
        @Json(name = "params")
        override val params: Session.SessionPayloadParams
    ) : PostSettlementSession() {
        val sessionParams = params.request.params
    }

    @JsonClass(generateAdapter = true)
    data class SessionDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_DELETE,
        @Json(name = "params")
        override val params: Session.DeleteParams
    ) : PostSettlementSession() {
        val message = params.reason.message
    }
}