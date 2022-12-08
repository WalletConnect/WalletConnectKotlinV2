package com.walletconnect.auth.client

import com.walletconnect.android.CoreClient

object Auth {

    sealed class Event {
        data class AuthRequest(
            val id: Long,
            val payloadParams: Model.PayloadParams
        ) : Event()

        data class AuthResponse(
            val id: Long,
            val response: Model.Response,
        ) : Event()

        data class ConnectionStateChange(
            val state: Model.ConnectionState,
        ) : Event()

        data class Error(
            val error: Model.Error
        ) : Event()
    }

    sealed class Model {

        data class Error(val throwable: Throwable) : Model()

        data class ConnectionState(val isAvailable: Boolean) : Model()

        data class PendingRequest(
            val id: Long,
            val payloadParams: PayloadParams
        ) : Model()

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
                val address: String
                val chainId: String

                init {
                    iss.split(ISS_DELIMITER).apply {
                        address = get(ISS_POSITION_OF_ADDRESS)
                        chainId = get(ISS_POSITION_OF_CHAIN_ID)
                    }
                }

                private companion object {
                    const val ISS_DELIMITER = ":"
                    const val ISS_POSITION_OF_CHAIN_ID = 3
                    const val ISS_POSITION_OF_ADDRESS = 4
                }
            }
        }

        sealed class Response : Model() {
            abstract val id: Long

            data class Result(override val id: Long, val cacao: Cacao) : Response()
            data class Error(override val id: Long, val code: Int, val message: String) : Response()
        }
    }

    sealed class Params {

        data class Init(val core: CoreClient) : Params()

        data class Request(
            val topic: String,
            val chainId: String,
            val domain: String,
            val nonce: String,
            val aud: String,
            val type: String?,
            val nbf: String?,
            val exp: String?,
            val statement: String?,
            val requestId: String?,
            val resources: List<String>?,
        ) : Params()

        sealed class Respond : Params() {
            abstract val id: Long

            data class Result(override val id: Long, val signature: Model.Cacao.Signature, val issuer: String) : Respond()
            data class Error(override val id: Long, val code: Int, val message: String) : Respond()
        }

        data class FormatMessage(val payloadParams: Model.PayloadParams, val issuer: String) : Params()
    }
}