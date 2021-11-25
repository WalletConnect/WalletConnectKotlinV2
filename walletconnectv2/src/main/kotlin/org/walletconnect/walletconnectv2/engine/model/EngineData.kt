package org.walletconnect.walletconnectv2.engine.model

import com.squareup.moshi.JsonClass
import java.net.URI

sealed class EngineData {

    internal data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        var methods: List<String>,
        val topic: String,
        val proposerPublicKey: String,
        val ttl: Long
    ) : EngineData() {
        val icon: String = icons.first().toString()
    }

    internal data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val request: JSONRPCRequest
    ) : EngineData() {

        data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String
        )
    }

    internal data class SettledSession(
        var icon: String?,
        var name: String,
        var uri: String,
        val topic: String
    ) : EngineData()


    sealed class JsonRpcResponse : EngineData() {
        abstract val id: Long
        val jsonrpc: String = "2.0"

        @JsonClass(generateAdapter = true)
        data class JsonRpcResult(
            override val id: Long,
            val result: String,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        data class JsonRpcError(
            override val id: Long,
            val error: Error,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        data class Error(
            val code: Long,
            val message: String,
        )
    }
}