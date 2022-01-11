package com.walletconnect.walletconnectv2.client

import com.walletconnect.walletconnectv2.util.Empty
import java.net.URI

data class SessionProposal(
    val name: String,
    val description: String,
    val url: String,
    val icons: List<URI>,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>,
    val topic: String,
    val proposerPublicKey: String,
    val ttl: Long,
    val accounts: List<String>
) : WalletConnectClient.Model() {
    val icon: String = icons.first().toString()
}

data class SessionRequest(
    val topic: String,
    val chainId: String?,
    val request: JSONRPCRequest
) : WalletConnectClient.Model() {

    data class JSONRPCRequest(
        val id: Long,
        val method: String,
        val params: String
    ) : WalletConnectClient.Model()
}

data class SettledSession(
    val topic: String,
    val accounts: List<String>,
    val peerAppMetaData: AppMetaData?,
    val permissions: Permissions
) : WalletConnectClient.Model() {

    data class Permissions(
        val blockchain: Blockchain,
        val jsonRpc: JsonRpc,
        val notifications: Notifications
    ) {
        data class Blockchain(val chains: List<String>)

        data class JsonRpc(val methods: List<String>)

        data class Notifications(val types: List<String>)
    }
}

data class SessionState(val accounts: List<String>) : WalletConnectClient.Model()

data class SettledPairing(val topic: String) : WalletConnectClient.Model()

data class RejectedSession(val topic: String, val reason: String) : WalletConnectClient.Model()

data class DeletedSession(val topic: String, val reason: String) : WalletConnectClient.Model()

data class UpgradedSession(val topic: String, val permissions: SessionPermissions) : WalletConnectClient.Model()

data class SessionPermissions(val blockchain: Blockchain? = null, val jsonRpc: Jsonrpc? = null) : WalletConnectClient.Model()

data class Blockchain(val chains: List<String>) : WalletConnectClient.Model()

data class Jsonrpc(val methods: List<String>) : WalletConnectClient.Model()

data class UpdatedSession(val topic: String, val accounts: List<String>) : WalletConnectClient.Model()

data class SessionNotification(
    val topic: String,
    val type: String,
    val data: String
) : WalletConnectClient.Model()

data class Notification(
    val type: String,
    val data: String
) : WalletConnectClient.Model()

sealed class JsonRpcResponse : WalletConnectClient.Model() {
    abstract val id: Long
    val jsonrpc: String = "2.0"

    data class JsonRpcResult(
        override val id: Long,
        val result: String
    ) : JsonRpcResponse()

    data class JsonRpcError(
        override val id: Long,
        val error: Error
    ) : JsonRpcResponse()

    data class Error(
        val code: Long,
        val message: String
    )
}

data class AppMetaData(
    val name: String = "Peer",
    val description: String = String.Empty,
    val url: String = String.Empty,
    val icons: List<String> = emptyList()
) : WalletConnectClient.Model()