package com.walletconnect.walletconnectv2.client

import android.app.Application
import android.net.Uri
import com.walletconnect.walletconnectv2.network.Relay
import java.net.URI

object WalletConnect {

    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    sealed class Model {

        data class Error(val error: Throwable) : Model()

        sealed class ProposedSequence {
            class Pairing(val uri: String) : ProposedSequence()
            object Session : ProposedSequence()
        }

        data class SessionProposal(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<URI>,
            val chains: List<String>,
            val methods: List<String>,
            val events: List<String>,
            val proposerPublicKey: String,
            val accounts: List<String>,
            val relayProtocol: String,
            val relayData: String?,
        ) : Model()

        data class SessionRequest(
            val topic: String,
            val chainId: String?,
            val peerMetaData: AppMetaData?,
            val request: JSONRPCRequest,
        ) : Model() {

            data class JSONRPCRequest(
                val id: Long,
                val method: String,
                val params: String,
            ) : Model()
        }

        data class Pairing(val topic: String, val metaData: AppMetaData?) : Model()

        sealed class SettledSessionResponse : Model() {
            data class Result(val session: Session) : SettledSessionResponse()
            data class Error(val errorMessage: String) : SettledSessionResponse()
        }

        sealed class SessionUpdateAccountsResponse : Model() {
            data class Result(val topic: String, val accounts: List<String>) : SessionUpdateAccountsResponse()
            data class Error(val errorMessage: String) : SessionUpdateAccountsResponse()
        }

        sealed class SessionUpdateMethodsResponse : Model() {
            data class Result(val topic: String, val methods: List<String>) : SessionUpdateMethodsResponse()
            data class Error(val errorMessage: String) : SessionUpdateMethodsResponse()
        }

        sealed class SessionUpdateEventsResponse : Model() {
            data class Result(val topic: String, val events: List<String>) : SessionUpdateEventsResponse()
            data class Error(val errorMessage: String) : SessionUpdateEventsResponse()
        }

        sealed class DeletedSession : Model() {
            data class Success(val topic: String, val reason: String) : DeletedSession()
            data class Error(val error: Throwable) : DeletedSession()
        }

        sealed class Ping : Model() {
            data class Success(val topic: String) : Ping()
            data class Error(val error: Throwable) : Ping()
        }

        data class RejectedSession(val topic: String, val reason: String) : Model()

        data class Blockchain(val chains: List<String>) : Model()

        data class UpdatedSessionAccounts(val topic: String, val accounts: List<String>) : Model()

        data class UpdatedSessionMethods(val topic: String, val methods: List<String>) : Model()

        data class UpdatedSessionEvents(val topic: String, val events: List<String>) : Model()

        data class ApprovedSession(
            val topic: String,
            val metaData: AppMetaData?,
            val methods: List<String>,
            val events: List<String>,
            val accounts: List<String>,
        ) : Model()

        data class Session(
            val topic: String,
            val expiry: Long,
            val accounts: List<String>,
            val methods: List<String>,
            val events: List<String>,
            val metaData: AppMetaData?,
        ) : Model()

        data class SessionEvent(
            val name: String,
            val data: String,
        ) : Model()

        data class SessionRequestResponse(
            val topic: String,
            val chainId: String?,
            val method: String,
            val result: JsonRpcResponse,
        ) : Model()

        sealed class JsonRpcResponse : Model() {
            abstract val id: Long
            val jsonrpc: String = "2.0"

            data class JsonRpcResult(
                override val id: Long,
                val result: String,
            ) : JsonRpcResponse()

            data class JsonRpcError(
                override val id: Long,
                val code: Int,
                val message: String,
            ) : JsonRpcResponse()
        }

        data class AppMetaData(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<String>,
        ) : Model()

        data class PendingRequest(
            val requestId: Long,
            val topic: String,
            val method: String,
            val chainId: String?,
            val params: String,
        ) : Model()
    }

    sealed class Params {

        // TODO: Maybe convert this into a Builder
        data class Init internal constructor(
            val application: Application,
            val metadata: Model.AppMetaData,
            val relay: Relay? = null
        ) : Params() {
            internal lateinit var serverUrl: String

            constructor(
                application: Application,
                useTls: Boolean,
                hostName: String,
                projectId: String,
                metadata: Model.AppMetaData,
                relay: Relay? = null
            ) : this(application, metadata, relay) {
                val relayServerUrl = Uri.Builder().scheme((if (useTls) "wss" else "ws"))
                    .authority(hostName)
                    .appendQueryParameter("projectId", projectId)
                    .build()
                    .toString()

                require(relayServerUrl.isValidRelayServerUrl()) {
                    "Check the schema and projectId parameter of the Server Url"
                }

                this.serverUrl = relayServerUrl
            }

            constructor(
                application: Application,
                relayServerUrl: String,
                metadata: Model.AppMetaData,
                relay: Relay? = null
            ) : this(application, metadata, relay) {
                require(relayServerUrl.isValidRelayServerUrl()) {
                    "Check the schema and projectId parameter of the Server Url"
                }

                this.serverUrl = relayServerUrl
            }

            private fun String.isValidRelayServerUrl(): Boolean {
                return this.isNotBlank() && Uri.parse(this)?.let { relayUrl ->
                    arrayOf("wss", "ws").contains(relayUrl.scheme) && !relayUrl.getQueryParameter("projectId").isNullOrBlank()
                } ?: false
            }
        }

        data class Connect(
            val chains: List<String>,
            val methods: List<String>,
            val events: List<String>,
            val pairingTopic: String? = null,
        ) : Params()

        data class Pair(val uri: String) : Params()

        data class Approve(val proposerPublicKey: String, val accounts: List<String>, val methods: List<String>, val events: List<String>) :
            Params()

        data class Reject(val proposerPublicKey: String, val reason: String, val code: Int) : Params()

        data class Disconnect(val sessionTopic: String, val reason: String, val reasonCode: Int) : Params()

        data class Response(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class Request(val sessionTopic: String, val method: String, val params: String, val chainId: String?) : Params()

        data class UpdateAccounts(val sessionTopic: String, val accounts: List<String>) : Params()

        data class UpdateMethods(val sessionTopic: String, val methods: List<String>) : Params()

        data class UpdateEvents(val sessionTopic: String, val events: List<String>) : Params()

        data class Ping(val topic: String) : Params()

        data class Emit(val topic: String, val event: Model.SessionEvent, val chainId: String?) : Params()

        data class UpdateExpiry(val topic: String, val newExpiration: Long) : Params()
    }
}