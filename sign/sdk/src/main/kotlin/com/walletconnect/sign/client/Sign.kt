package com.walletconnect.sign.client

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import java.net.URI

object Sign {

    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    @Deprecated(
        message = "ConnectionType for the relay is moved to CoreClient",
        replaceWith = ReplaceWith(expression = "ConnectionType", imports = ["com.walletconnect.android.relay"])
    )
    enum class ConnectionType {
        AUTOMATIC, MANUAL
    }

    sealed class Model {

        data class Error(val throwable: Throwable) : Model()

        sealed class ProposedSequence : Model() {
            class Pairing(val uri: String) : ProposedSequence()
            object Session : ProposedSequence()
        }

        data class SessionProposal(
            val pairingTopic: String,
            val name: String,
            val description: String,
            val url: String,
            val icons: List<URI>,
            val redirect: String,
            val requiredNamespaces: Map<String, Namespace.Proposal>,
            val optionalNamespaces: Map<String, Namespace.Proposal>,
            val properties: Map<String, String>?,
            val proposerPublicKey: String,
            val relayProtocol: String,
            val relayData: String?,
        ) : Model()

        data class SessionRequest(
            val topic: String,
            val chainId: String?,
            val peerMetaData: Core.Model.AppMetaData?,
            val request: JSONRPCRequest,
        ) : Model() {

            data class JSONRPCRequest(
                val id: Long,
                val method: String,
                val params: String,
            ) : Model()
        }

        data class SentRequest(
            val requestId: Long,
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String
        ) : Params()

        sealed class Namespace : Model() {

            //Required or Optional
            data class Proposal(
                val chains: List<String>? = null,
                val methods: List<String>,
                val events: List<String>
            ) : Namespace()

            data class Session(
                val chains: List<String>? = null,
                val accounts: List<String>,
                val methods: List<String>,
                val events: List<String>
            ) : Namespace()
        }

        @Deprecated(message = "RelayProtocolOptions is deprecated")
        data class RelayProtocolOptions(val protocol: String, val data: String? = null) : Model()

        data class Pairing(val topic: String, val metaData: Core.Model.AppMetaData?) : Model()

        sealed class SettledSessionResponse : Model() {
            data class Result(val session: Session) : SettledSessionResponse()
            data class Error(val errorMessage: String) : SettledSessionResponse()
        }

        sealed class SessionUpdateResponse : Model() {
            data class Result(val topic: String, val namespaces: Map<String, Namespace.Session>) : SessionUpdateResponse()
            data class Error(val errorMessage: String) : SessionUpdateResponse()
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

        data class UpdatedSession(
            val topic: String,
            val namespaces: Map<String, Namespace.Session>
        ) : Model()

        data class ApprovedSession(
            val topic: String,
            val metaData: Core.Model.AppMetaData?,
            val namespaces: Map<String, Namespace.Session>,
            val accounts: List<String>,
        ) : Model()

        data class Session(
            val pairingTopic: String,
            val topic: String,
            val expiry: Long,
            val namespaces: Map<String, Namespace.Session>,
            val metaData: Core.Model.AppMetaData?,
        ) : Model() {
            val redirect: String? = metaData?.redirect
        }

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

        data class PendingRequest(
            val requestId: Long,
            val topic: String,
            val method: String,
            val chainId: String?,
            val params: String,
        ) : Model()

        data class ConnectionState(
            val isAvailable: Boolean,
        ) : Model()
    }

    sealed class Params {

        data class Init constructor(
            val core: CoreClient
        ) : Params()

        data class Connect(
            val namespaces: Map<String, Model.Namespace.Proposal>? = null,
            val optionalNamespaces: Map<String, Model.Namespace.Proposal>? = null,
            val properties: Map<String, String>? = null,
            val pairing: Core.Model.Pairing
        ) : Params()

        data class Pair(val uri: String) : Params()

        data class Approve(
            val proposerPublicKey: String,
            val namespaces: Map<String, Model.Namespace.Session>,
            val relayProtocol: String? = null,
        ) : Params()

        data class Reject(val proposerPublicKey: String, val reason: String) : Params()

        data class Disconnect(val sessionTopic: String) : Params()

        data class Response(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class Request(
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String,
            val expiry: Long? = null
        ) : Params()

        data class Update(
            val sessionTopic: String,
            val namespaces: Map<String, Model.Namespace.Session>,
        ) : Params()

        data class Ping(val topic: String) : Params()

        data class Emit(val topic: String, val event: Model.SessionEvent, val chainId: String) :
            Params()

        data class Extend(val topic: String) : Params()
    }
}