package com.walletconnect.wcmodal.client

import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import java.net.URI

object Modal {

    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    sealed class Params {
        data class Init(
            val core: CoreClient,
            val excludedWalletIds: List<String> = listOf(),
            val recommendedWalletsIds: List<String> = listOf(),
            val sessionParams: SessionParams? = null
        ) : Params()

        data class Connect(
            val namespaces: Map<String, Model.Namespace.Proposal>? = null,
            val optionalNamespaces: Map<String, Model.Namespace.Proposal>? = null,
            val properties: Map<String, String>? = null,
            val pairing: Core.Model.Pairing
        ) : Params()

        data class Authenticate(
            val pairingTopic: String,
            val payloadParams: Model.PayloadParams
        ) : Params()

        data class Disconnect(val sessionTopic: String) : Params()

        data class Ping(val topic: String) : Params()

        data class Request(
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String,
            val expiry: Long? = null
        ) : Params()

        data class SessionParams(
            val requiredNamespaces: Map<String, Model.Namespace.Proposal>,
            val optionalNamespaces: Map<String, Model.Namespace.Proposal>? = null,
            val properties: Map<String, String>? = null
        )
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

        data class PayloadParams(
            val type: String,
            val chains: List<String>,
            val domain: String,
            val aud: String,
            val version: String,
            val nonce: String,
            val iat: String,
            val nbf: String?,
            val exp: String?,
            val statement: String?,
            val requestId: String?,
            val resources: List<String>?,
        ) : Model()

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

        data class ExpiredProposal(val pairingTopic: String, val proposerPublicKey: String) : Model()

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

        data class ExpiredRequest(val topic: String, val id: Long) : Model()

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

        sealed class SessionAuthenticateResponse : Model() {
            data class Result(val id: Long, val cacaos: List<Cacao>) : SessionAuthenticateResponse()
            data class Error(val id: Long, val code: Int, val message: String) : SessionAuthenticateResponse()
        }

        data class Cacao(
            val header: Header,
            val payload: Payload,
            val signature: Signature,
        ) : Model() {
            @Keep
            data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface
            data class Header(val t: String) : Model()
            data class Payload(
                val iss: String,
                val domain: String,
                val aud: String,
                val version: String,
                val nonce: String,
                val iat: String,
                val nbf: String?,
                val exp: String?,
                val statement: String?,
                val requestId: String?,
                val resources: List<String>?,
            ) : Model() {
                val address: String get() = Issuer(iss).address
            }
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