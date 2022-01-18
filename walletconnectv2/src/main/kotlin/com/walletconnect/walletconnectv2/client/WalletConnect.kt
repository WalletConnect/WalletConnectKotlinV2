package com.walletconnect.walletconnectv2.client

import android.app.Application
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Empty
import org.json.JSONObject
import java.net.URI

object WalletConnect {

    sealed interface Listeners {
        fun onError(error: Throwable)

        interface Pairing : Listeners {
            fun onSuccess(settledPairing: Model.SettledPairing)
        }

        interface SessionReject : Listeners {
            fun onSuccess(rejectedSession: Model.RejectedSession)
        }

        interface SessionDelete : Listeners {
            fun onSuccess(deletedSession: Model.DeletedSession)
        }

        interface SessionApprove : Listeners {
            fun onSuccess(settledSession: Model.SettledSession)
        }

        interface SessionPayload : Listeners

        interface SessionUpdate : Listeners {
            fun onSuccess(updatedSession: Model.UpdatedSession)
        }

        interface SessionUpgrade : Listeners {
            fun onSuccess(upgradedSession: Model.UpgradedSession)
        }

        interface SessionPing : Listeners {
            fun onSuccess(topic: String)
        }

        interface Notification : Listeners {
            fun onSuccess(topic: String)
        }
    }

    sealed class Model {

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
            val isController: Boolean,
            val ttl: Long,
            val accounts: List<String>
        ) : Model() {
            val icon: String = icons.first().toString()
        }

        data class SessionRequest(
            val topic: String,
            val chainId: String?,
            val request: JSONRPCRequest
        ) : Model() {

            data class JSONRPCRequest(
                val id: Long,
                val method: String,
                val params: String
            ) : Model()
        }

        data class SettledSession(
            val topic: String,
            val accounts: List<String>,
            val peerAppMetaData: AppMetaData?,
            val permissions: Permissions
        ) : Model() {

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

        data class SessionState(val accounts: List<String>) : Model()

        data class SettledPairing(
            val topic: String,
            val permissions: SessionPermissions = SessionPermissions()
        ) : Model()

        data class RejectedSession(val topic: String, val reason: String) : Model()

        data class DeletedSession(val topic: String, val reason: String) : Model()

        data class UpgradedSession(val topic: String, val permissions: SessionPermissions) : Model()

        data class SessionPermissions(val blockchain: Blockchain? = null, val jsonRpc: Jsonrpc? = null) : Model()

        data class Blockchain(val chains: List<String>) : Model()

        data class Jsonrpc(val methods: List<String>) : Model()

        data class UpdatedSession(val topic: String, val accounts: List<String>) : Model()

        data class SessionNotification(
            val topic: String,
            val type: String,
            val data: String
        ) : Model()

        data class Notification(
            val type: String,
            val data: String
        ) : Model()

        sealed class JsonRpcResponse : Model() {
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
        ) : Model()
    }

    sealed class Params {

        data class Init(
            val application: Application,
            val useTls: Boolean = true,
            val hostName: String = WALLET_CONNECT_URL,
            val projectId: String = "",
            val isController: Boolean = true,
            val metadata: Model.AppMetaData = Model.AppMetaData()
        ) : Params()

        data class Pair(val uri: String) : Params()

        data class Approve(val proposal: Model.SessionProposal, val accounts: List<String>) : Params()

        data class Reject(val rejectionReason: String, val proposalTopic: String) : Params()

        data class Disconnect(val sessionTopic: String, val reason: String) : Params()

        data class Response(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class Update(val sessionTopic: String, val sessionState: Model.SessionState) : Params()

        data class Upgrade(val topic: String, val permissions: Model.SessionPermissions) : Params()

        data class Ping(val topic: String) : Params()

        data class Notify(val topic: String, val notification: Model.Notification) : Params()

        companion object {
            internal const val WALLET_CONNECT_URL = "relay.walletconnect.com"
        }
    }
}