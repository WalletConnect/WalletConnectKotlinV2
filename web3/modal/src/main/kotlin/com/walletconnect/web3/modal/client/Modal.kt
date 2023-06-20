package com.walletconnect.web3.modal.client

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.CoreInterface

object Modal {

    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }
    sealed class Params {
        data class Init(
            val core: CoreInterface<CoreClient.CoreDelegate>
        ) : Params()

        data class Connect(
            val namespaces: Map<String, Model.Namespace.Proposal>? = null,
            val optionalNamespaces: Map<String, Model.Namespace.Proposal>? = null,
            val properties: Map<String, String>? = null,
            val pairing: Core.Model.Pairing
        ): Params()

        data class Disconnect(val sessionTopic: String) : Params()

        data class Ping(val topic: String) : Params()

        data class Request(
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String,
            val expiry: Long? = null
        ) : Params()
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

        sealed class Namespace : Model() {
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

        data class ApprovedSession(
            val topic: String,
            val metaData: Core.Model.AppMetaData?,
            val namespaces: Map<String, Namespace.Session>,
            val accounts: List<String>,
        ) : Model()

        data class RejectedSession(val topic: String, val reason: String) : Model()

        data class UpdatedSession(
            val topic: String,
            val namespaces: Map<String, Namespace.Session>
        ) : Model()

        data class SessionEvent(
            val name: String,
            val data: String,
        ) : Model()

        sealed class DeletedSession : Model() {
            data class Success(val topic: String, val reason: String) : DeletedSession()
            data class Error(val error: Throwable) : DeletedSession()
        }

        data class Session(
            val pairingTopic: String,
            val topic: String,
            val expiry: Long,
            val namespaces: Map<String, Namespace.Session>,
            val metaData: Core.Model.AppMetaData?,
        ) : Model() {
            val redirect: String? = metaData?.redirect
        }

        data class SessionRequestResponse(
            val topic: String,
            val chainId: String?,
            val method: String,
            val result: JsonRpcResponse,
        ) : Model()

        data class ConnectionState(
            val isAvailable: Boolean,
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

        data class SentRequest(
            val requestId: Long,
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String
        ) : Model()

        sealed class Ping : Model() {
            data class Success(val topic: String) : Ping()
            data class Error(val error: Throwable) : Ping()
        }
    }
}