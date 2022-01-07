package com.walletconnect.walletconnectv2.engine.model

import com.walletconnect.walletconnectv2.util.Empty
import java.net.URI

sealed class EngineModel {

    internal data class SessionProposalDO(
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
    ) : EngineModel() {
        val icon: String = icons.first().toString()
    }

    internal data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val request: JSONRPCRequest
    ) : EngineModel() {

        data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String
        ) : EngineModel()
    }

    internal data class DeletedSession(
        val topic: String,
        val reason: String
    ) : EngineModel()

    internal data class SettledSession(
        val topic: String,
        val accounts: List<String>,
        val peerAppMetaData: AppMetaDataDO?,
        val permissions: Permissions
    ) : EngineModel() {

        internal data class Permissions(
            val blockchain: Blockchain,
            val jsonRpc: JsonRpc,
            val notifications: Notifications
        ) {
            internal data class Blockchain(val chains: List<String>)

            internal data class JsonRpc(val methods: List<String>)

            internal data class Notifications(val types: List<String>)
        }
    }

    internal data class SessionNotification(
        val topic: String,
        val type: String,
        val data: String
    ) : EngineModel()

    internal data class Notification(
        val type: String,
        val data: String
    ) : EngineModel()

    internal data class SessionState(val accounts: List<String>) : EngineModel()

    internal data class SessionPermissions(val blockchain: Blockchain? = null, val jsonRpc: Jsonrpc? = null) : EngineModel()

    internal data class Blockchain(val chains: List<String>) : EngineModel()

    internal data class Jsonrpc(val methods: List<String>) : EngineModel()

    internal data class AppMetaDataDO(
        val name: String = "Peer",
        val description: String = String.Empty,
        val url: String = String.Empty,
        val icons: List<String> = emptyList()
    ) : EngineModel()
}