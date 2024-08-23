package com.walletconnect.web3.wallet.client

import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object Wallet {

    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    sealed class Params {
        data class Init(val core: CoreInterface) : Params()

        data class Pair(val uri: String) : Params()

        data class SessionApprove(
            val proposerPublicKey: String,
            val namespaces: Map<String, Model.Namespace.Session>,
            val relayProtocol: String? = null,
        ) : Params()

        data class ApproveSessionAuthenticate(val id: Long, val auths: List<Model.Cacao>) : Params()

        data class RejectSessionAuthenticate(val id: Long, val reason: String) : Params()

        data class SessionReject(val proposerPublicKey: String, val reason: String) : Params()

        data class SessionUpdate(val sessionTopic: String, val namespaces: Map<String, Model.Namespace.Session>) : Params()

        data class SessionExtend(val topic: String) : Params()

        data class SessionEmit(val topic: String, val event: Model.SessionEvent, val chainId: String) : Params()

        data class SessionRequestResponse(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class SessionDisconnect(val sessionTopic: String) : Params()

        data class FormatMessage(val payloadParams: Model.PayloadParams, val issuer: String) : Params()

        data class FormatAuthMessage(val payloadParams: Model.PayloadAuthRequestParams, val issuer: String) : Params()

        data class Ping(val sessionTopic: String, val timeout: Duration = 30.seconds) : Params()

        sealed class AuthRequestResponse : Params() {
            abstract val id: Long

            data class Result(override val id: Long, val signature: Model.Cacao.Signature, val issuer: String) : AuthRequestResponse()
            data class Error(override val id: Long, val code: Int, val message: String) : AuthRequestResponse()
        }

        data class DecryptMessage(val topic: String, val encryptedMessage: String) : Params()
    }

    sealed class Model {

        sealed class Ping : Model() {
            data class Success(val topic: String) : Ping()
            data class Error(val error: Throwable) : Ping()
        }
        data class Error(val throwable: Throwable) : Model()

		data class ConnectionState(val isAvailable: Boolean, val reason: Reason? = null) : Model() {
			sealed class Reason : Model() {
				data class ConnectionClosed(val message: String) : Reason()
				data class ConnectionFailed(val throwable: Throwable) : Reason()
			}
		}

        data class ExpiredProposal(val pairingTopic: String, val proposerPublicKey: String) : Model()
        data class ExpiredRequest(val topic: String, val id: Long) : Model()

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

        data class SessionAuthenticate(
            val id: Long,
            val pairingTopic: String,
            val participant: Participant,
            val payloadParams: PayloadAuthRequestParams,
        ) : Model() {
            data class Participant(
                val publicKey: String,
                val metadata: Core.Model.AppMetaData?,
            ) : Model()
        }

        data class VerifyContext(
            val id: Long,
            val origin: String,
            val validation: Validation,
            val verifyUrl: String,
            val isScam: Boolean?,
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
            val payloadParams: PayloadParams,
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
                val events: List<String>,
            ) : Namespace()

            data class Session(
                val chains: List<String>? = null,
                val accounts: List<String>,
                val methods: List<String>,
                val events: List<String>,
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

        data class PayloadAuthRequestParams(
            val chains: List<String>,
            val domain: String,
            val nonce: String,
            val aud: String,
            val type: String?,
            val iat: String,
            val nbf: String?,
            val exp: String?,
            val statement: String?,
            val requestId: String?,
            val resources: List<String>?
        ) : Model()

        data class SessionEvent(
            val name: String,
            val data: String,
        ) : Model()

        data class Event(
            val topic: String,
            val name: String,
            val data: String,
            val chainId: String,
        ) : Model()

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
            @Deprecated("Pairing topic is deprecated")
            val pairingTopic: String,
            val topic: String,
            val expiry: Long,
            val requiredNamespaces: Map<String, Namespace.Proposal>,
            val optionalNamespaces: Map<String, Namespace.Proposal>?,
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
            val payloadParams: PayloadParams,
        ) : Model()

        sealed class Message : Model() {

            data class Simple(
                val title: String,
                val body: String,
            ) : Message()

            data class SessionProposal(
                val id: Long,
                val pairingTopic: String,
                val name: String,
                val description: String,
                val url: String,
                val icons: List<String>,
                val redirect: String,
                val requiredNamespaces: Map<String, Namespace.Proposal>,
                val optionalNamespaces: Map<String, Namespace.Proposal>,
                val properties: Map<String, String>?,
                val proposerPublicKey: String,
                val relayProtocol: String,
                val relayData: String?,
            ) : Message()

            data class SessionRequest(
                val topic: String,
                val chainId: String?,
                val peerMetaData: Core.Model.AppMetaData?,
                val request: JSONRPCRequest,
            ) : Message() {
                data class JSONRPCRequest(
                    val id: Long,
                    val method: String,
                    val params: String,
                ) : Message()
            }

            data class AuthRequest(
                val id: Long,
                val pairingTopic: String,
                val metadata: Core.Model.AppMetaData,
                val payloadParams: PayloadParams,
            ) : Message() {
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
                )
            }
        }
    }
}