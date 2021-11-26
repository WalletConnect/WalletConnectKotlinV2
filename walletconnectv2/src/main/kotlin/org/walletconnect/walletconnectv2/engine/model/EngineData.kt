package org.walletconnect.walletconnectv2.engine.model

import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
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
        val ttl: Long,
        val accounts: List<String>
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
        ) : EngineData()
    }

    internal data class DeletedSession(
        val topic: String,
        val reason: String
    ) : EngineData()

    internal data class SettledSession(
        var icon: String?,
        var name: String,
        var uri: String,
        val topic: String
    ) : EngineData()

    internal data class SessionNotification(
        val topic: String,
        val notification: Notification
    ) : EngineData()

    internal data class Notification(
        val type: String,
        val data: Any
    ) : EngineData()

    data class SessionState(val accounts: List<String>) : EngineData()

    data class SessionPermissions(val blockchain: Blockchain? = null, val jsonRpc: Jsonrpc? = null) : EngineData()

    data class Blockchain(val chains: List<String>) : EngineData()

    data class Jsonrpc(val methods: List<String>) : EngineData()

    internal sealed class JsonRpcResponse : EngineData() {
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