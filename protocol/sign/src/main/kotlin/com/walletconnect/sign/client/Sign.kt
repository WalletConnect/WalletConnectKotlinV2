package com.walletconnect.sign.client

import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.model.Participant
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

        data class ExpiredProposal(val pairingTopic: String, val proposerPublicKey: String) : Model()
        data class ExpiredRequest(val topic: String, val id: Long) : Model()

        data class VerifyContext(
            val id: Long,
            val origin: String,
            val validation: Validation,
            val verifyUrl: String,
            val isScam: Boolean?
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

        data class SentRequest(
            val requestId: Long,
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String,
        ) : Params()

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

        sealed class SessionAuthenticateResponse : Model() {
            data class Result(val id: Long, val cacaos: List<Cacao>, val session: Session) : SessionAuthenticateResponse()
            data class Error(val id: Long, val code: Int, val message: String) : SessionAuthenticateResponse()
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
            val namespaces: Map<String, Namespace.Session>,
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
            val requiredNamespaces: Map<String, Namespace.Proposal>,
            val optionalNamespaces: Map<String, Namespace.Proposal>?,
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

        sealed class Message : Model() {
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
        }

        data class SessionAuthenticate(
            val id: Long,
            val topic: String,
            val participant: Participant,
            val payloadParams: PayloadParams
        ) {
            data class Participant(
                val publicKey: String,
                val metadata: Core.Model.AppMetaData?,
            ) : Model()
        }

        data class PayloadParams(
            val chains: List<String>,
            val domain: String,
            val nonce: String,
            val aud: String,
            val type: String?,
            val nbf: String?,
            val iat: String,
            val exp: String?,
            val statement: String?,
            val requestId: String?,
            var resources: List<String>?,
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
                val address: String get() = Issuer(iss).address
            }
        }
    }

    sealed class Params {

        data class Init constructor(
            val core: CoreInterface,
        ) : Params()

        data class Connect(
            val namespaces: Map<String, Model.Namespace.Proposal>? = null,
            val optionalNamespaces: Map<String, Model.Namespace.Proposal>? = null,
            val properties: Map<String, String>? = null,
            val pairing: Core.Model.Pairing,
        ) : Params()

        data class Authenticate(
            val pairingTopic: String? = null,
            val chains: List<String>,
            val domain: String,
            val nonce: String,
            val aud: String,
            val type: String?,
            val nbf: String?,
            val exp: String?,
            val statement: String?,
            val requestId: String?,
            val resources: List<String>?,
            val methods: List<String>?
        ) : Params()

        data class FormatMessage(val payloadParams: Model.PayloadParams, val iss: String) : Params()

        data class Pair(val uri: String) : Params()

        data class Approve(
            val proposerPublicKey: String,
            val namespaces: Map<String, Model.Namespace.Session>,
            val relayProtocol: String? = null,
        ) : Params()

        data class Reject(val proposerPublicKey: String, val reason: String) : Params()

        data class ApproveSessionAuthenticate(val id: Long, val cacaos: List<Model.Cacao>) : Params()
        data class RejectSessionAuthenticate(val id: Long, val reason: String) : Params()

        data class Disconnect(val sessionTopic: String) : Params()

        data class Response(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class Request(
            val sessionTopic: String,
            val method: String,
            val params: String,
            val chainId: String,
            val expiry: Long? = null,
        ) : Params()

        data class Update(
            val sessionTopic: String,
            val namespaces: Map<String, Model.Namespace.Session>,
        ) : Params()

        data class Ping(val topic: String, val timeout: Duration = 30.seconds) : Params()

        data class Emit(val topic: String, val event: Model.SessionEvent, val chainId: String) :
            Params()

        data class Extend(val topic: String) : Params()

        data class DecryptMessage(val topic: String, val encryptedMessage: String) : Params()
    }
}