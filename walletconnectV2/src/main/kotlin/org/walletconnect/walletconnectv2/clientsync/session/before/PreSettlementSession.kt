package org.walletconnect.walletconnectv2.clientsync.session.before

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod

sealed class PreSettlementSession {
    abstract val id: Long
    abstract val jsonrpc: String
    abstract val method: String
    abstract val params: Session

    @JsonClass(generateAdapter = true)
    data class Proposal(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.wcSessionPropose,
        @Json(name = "params")
        override val params: Session.Proposal
    ) : PreSettlementSession()

    @JsonClass(generateAdapter = true)
    data class Approve(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.wcSessionApprove,
        @Json(name = "params")
        override val params: Session.Success
    ) : PreSettlementSession()

    data class Reject(
        override val id: Long,
        override val jsonrpc: String = "2.0",
        override val method: String = JsonRpcMethod.wcSessionReject,
        override val params: Session.Failure
    ) : PreSettlementSession()

}