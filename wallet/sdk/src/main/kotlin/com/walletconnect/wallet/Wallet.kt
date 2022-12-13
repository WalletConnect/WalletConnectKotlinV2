package com.walletconnect.wallet

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient

object Wallet {

    sealed class Params {
        data class Init constructor(val core: CoreClient) : Params()

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

        sealed class Namespace : Model() {

            data class Proposal(
                val chains: List<String>,
                val methods: List<String>,
                val events: List<String>,
                val extensions: List<Extension>?,
            ) : Namespace() {

                data class Extension(
                    val chains: List<String>,
                    val methods: List<String>,
                    val events: List<String>
                )
            }

            data class Session(
                val accounts: List<String>,
                val methods: List<String>,
                val events: List<String>,
                val extensions: List<Extension>?,
            ) : Namespace() {

                data class Extension(
                    val accounts: List<String>,
                    val methods: List<String>,
                    val events: List<String>
                )
            }
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
            data class Signature(val t: String, val s: String, val m: String? = null) : Model()
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
            val payloadParams: PayloadParams
        ) : Model()
    }
}