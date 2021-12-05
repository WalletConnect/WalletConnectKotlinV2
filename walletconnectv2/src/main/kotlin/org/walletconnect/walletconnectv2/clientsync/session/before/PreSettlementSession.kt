package org.walletconnect.walletconnectv2.clientsync.session.before

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.jsonrpc.utils.JsonRpcMethod

sealed class PreSettlementSession : ClientSyncJsonRpc {
    abstract override val id: Long
    abstract override val method: String
    abstract val jsonrpc: String
    abstract val params: Session

    @JsonClass(generateAdapter = true)
    data class Proposal(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PROPOSE,
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
        override val method: String = JsonRpcMethod.WC_SESSION_APPROVE,
        @Json(name = "params")
        override val params: Session.Success
    ) : PreSettlementSession()

    data class Reject(
        override val id: Long,
        override val jsonrpc: String = "2.0",
        override val method: String = JsonRpcMethod.WC_SESSION_REJECT,
        override val params: Session.Failure
    ) : PreSettlementSession()

}