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

    internal data class SessionRequestDO(
        val topic: String,
        val chainId: String?,
        val request: JSONRPCRequestDO
    ) : EngineModel() {

        data class JSONRPCRequestDO(
            val id: Long,
            val method: String,
            val params: String
        ) : EngineModel()
    }

    internal data class DeletedSessionDO(
        val topic: String,
        val reason: String
    ) : EngineModel()

    internal data class SettledSessionDO(
        val topic: String,
        val accounts: List<String>,
        val peerAppMetaData: AppMetaDataDO?,
        val permissions: PermissionsDO
    ) : EngineModel() {

        internal data class PermissionsDO(
            val blockchain: BlockchainDO,
            val jsonRpc: JsonRpcDO,
            val notifications: NotificationsDO
        ) {
            internal data class BlockchainDO(val chains: List<String>)

            internal data class JsonRpcDO(val methods: List<String>)

            internal data class NotificationsDO(val types: List<String>)
        }
    }

    internal data class SessionNotificationDO(
        val topic: String,
        val type: String,
        val data: String
    ) : EngineModel()

    internal data class NotificationDO(
        val type: String,
        val data: String
    ) : EngineModel()

    internal data class SessionStateDO(val accounts: List<String>) : EngineModel()

    internal data class SessionPermissionsDO(val blockchain: BlockchainDO? = null, val jsonRpc: JsonRpcDO? = null) : EngineModel()

    internal data class BlockchainDO(val chains: List<String>) : EngineModel()

    internal data class JsonRpcDO(val methods: List<String>) : EngineModel()

    internal data class AppMetaDataDO(
        val name: String = "Peer",
        val description: String = String.Empty,
        val url: String = String.Empty,
        val icons: List<String> = emptyList()
    ) : EngineModel()
}