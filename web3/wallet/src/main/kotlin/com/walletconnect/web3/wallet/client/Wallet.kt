package com.walletconnect.web3.wallet.client

import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.sign.client.Sign
import java.net.URI

object Wallet {

    sealed class Params {
        data class Init constructor(val core: CoreClient) : Params()

        data class Pair(val uri: String) : Params()

        data class SessionApprove(
            val proposerPublicKey: String,
            val namespaces: Map<String, Model.Namespace.Session>,
            val relayProtocol: String? = null,
        ) : Params()

        data class SessionReject(val proposerPublicKey: String, val reason: String) : Params()

        data class SessionUpdate(val sessionTopic: String, val namespaces: Map<String, Model.Namespace.Session>) : Params()

        data class SessionExtend(val topic: String) : Params()

        data class SessionEmit(val topic: String, val event: Model.SessionEvent, val chainId: String) : Params()

        data class SessionRequestResponse(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class SessionDisconnect(val sessionTopic: String) : Params()

        data class FormatMessage(val payloadParams: Model.PayloadParams, val issuer: String) : Params()

        sealed class AuthRequestResponse : Params() {
            abstract val id: Long

            data class Result(override val id: Long, val signature: Model.Cacao.Signature, val issuer: String) : AuthRequestResponse()
            data class Error(override val id: Long, val code: Int, val message: String) : AuthRequestResponse()
        }
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

        data class ConnectionState(val isAvailable: Boolean) : Model()

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

        data class SessionContext(
            val origin: String,
            val validation: Validation,
            val verifyUrl: String
        ) : Model()

        enum class Validation {
            VALID, INVALID, UNKNOWN
        }

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

        data class AuthRequest(
            val id: Long,
            val pairingTopic: String,
            val payloadParams: PayloadParams
        ) : Model()

        sealed class SettledSessionResponse : Model() {
            data class Result(val session: Session) : SettledSessionResponse()
            data class Error(val errorMessage: String) : SettledSessionResponse()
        }

        sealed class SessionUpdateResponse : Model() {
            data class Result(val topic: String, val namespaces: Map<String, Namespace.Session>) : SessionUpdateResponse()
            data class Error(val errorMessage: String) : SessionUpdateResponse()
        }

        sealed class SessionDelete : Model() {
            data class Success(val topic: String, val reason: String) : SessionDelete()
            data class Error(val error: Throwable) : SessionDelete()
        }

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

        data class PayloadParams(
            val type: String,
            val chainId: String,
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

        data class SessionEvent(val name: String, val data: String) : Model()

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
                val address: String get() = iss.split(ISS_DELIMITER)[ISS_POSITION_OF_ADDRESS]

                private companion object {
                    const val ISS_DELIMITER = ":"
                    const val ISS_POSITION_OF_ADDRESS = 4
                }
            }
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

        data class PendingSessionRequest(
            val requestId: Long,
            val topic: String,
            val method: String,
            val chainId: String?,
            val params: String,
        ) : Model()

        data class PendingAuthRequest(
            val id: Long,
            val pairingTopic: String,
            val payloadParams: PayloadParams
        ) : Model()
    }
}