package com.walletconnect.walletconnectv2.relay.model

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.type.ClientParams
import com.walletconnect.walletconnectv2.common.model.type.ClientSyncJsonRpc

sealed class RelayDO {

    internal sealed class JsonRpcResponse : RelayDO(), ClientSyncJsonRpc {
        abstract override val id: Long
        val jsonrpc: String = "2.0"

        @JsonClass(generateAdapter = true)
        data class JsonRpcResult(
            override val id: Long,
            val result: String
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        data class JsonRpcError(
            override val id: Long,
            val error: Error,
        ) : JsonRpcResponse()

        data class Error(
            val code: Long,
            val message: String,
        )
    }

    internal data class ClientJsonRpc(
        val id: Long,
        val jsonrpc: String,
        val method: String
    ) : RelayDO()

    internal data class WCRequestSubscriptionPayload(
        val requestId: Long,
        val topic: TopicVO,
        val method: String,
        val params: ClientParams
    ) : RelayDO()

    data class Acknowledgement(
        val id: Long,
        val jsonrpc: String = "2.0",
        val result: Boolean
    ) : RelayDO()
}
