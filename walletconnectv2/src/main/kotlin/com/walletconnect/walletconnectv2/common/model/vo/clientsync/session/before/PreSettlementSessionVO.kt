package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.type.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.relay.model.utils.JsonRpcMethod

internal sealed class PreSettlementSessionVO : ClientSyncJsonRpc {
    abstract override val id: Long
    abstract val method: String
    abstract val jsonrpc: String
    abstract val params: SessionParamsVO

    @JsonClass(generateAdapter = true)
    internal data class Proposal(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_PROPOSE,
        @Json(name = "params")
        override val params: SessionParamsVO.Proposal
    ) : PreSettlementSessionVO()

    @JsonClass(generateAdapter = true)
    internal data class Approve(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SESSION_APPROVE,
        @Json(name = "params")
        override val params: SessionParamsVO.Success
    ) : PreSettlementSessionVO() {
        val accounts: List<String> = params.state.accounts
        val expiry: Long = params.expiry.seconds
    }

    internal data class Reject(
        override val id: Long,
        override val jsonrpc: String = "2.0",
        override val method: String = JsonRpcMethod.WC_SESSION_REJECT,
        override val params: SessionParamsVO.Failure
    ) : PreSettlementSessionVO()
}