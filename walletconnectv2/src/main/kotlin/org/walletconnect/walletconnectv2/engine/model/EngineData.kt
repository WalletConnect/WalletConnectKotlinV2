package org.walletconnect.walletconnectv2.engine.model

import org.walletconnect.walletconnectv2.common.AppMetaData
import java.net.URI

sealed class EngineData {

    internal data class SessionProposal(
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
        val topic: String,
        val peerAppMetaData: AppMetaData?,
        val permissions: Permissions
    ) : EngineData() {

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

    internal data class SessionNotification(
        val topic: String,
        val type: String,
        val data: String
    ) : EngineData()

    internal data class Notification(
        val type: String,
        val data: String
    ) : EngineData()

    data class SessionState(val accounts: List<String>) : EngineData()

    data class SessionPermissions(val blockchain: Blockchain? = null, val jsonRpc: Jsonrpc? = null) : EngineData()

    data class Blockchain(val chains: List<String>) : EngineData()

    data class Jsonrpc(val methods: List<String>) : EngineData()
}