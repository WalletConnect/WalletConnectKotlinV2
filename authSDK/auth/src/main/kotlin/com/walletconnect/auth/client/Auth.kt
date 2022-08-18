package com.walletconnect.auth.client

import android.app.Application

object Auth {

    sealed interface Listeners {

    }

    sealed class Model {

        data class Error(val throwable: Throwable) : Model() // TODO: Should this be extracted to core for easier error handling?

        data class AppMetaData(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<String>,
            val redirect: String?,
        ) : Model()

        data class PendingRequest(
            val id: Long,
            val payloadParams: PayloadParams,
            val message: String,
        ) : Model() {

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
                val requestId: String,
                val resources: List<String>?,
            )
        }

        sealed class Events {
            data class AuthRequest(
                val id: Long,
                val message: String,
            ) : Events()

            data class AuthResponse(
                val id: Long,
                val response: Response,
            ) : Events()
        }


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
            data class Result(val cacao: Cacao) : Response()
            data class Error(val code: Int, val message: String) : Response()
        }
    }

    sealed class Params {

        data class Init(val application: Application, val appMetaData: Model.AppMetaData, val iss: String?) : Params()

        data class Pair(val uri: String) : Params()

        data class Request(
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
            data class Result(val id: Long, val signature: Model.Cacao.Signature) : Respond()
            data class Error(val code: Int, val message: String) : Respond()
        }

        data class RequestId(val id: Long)
    }
}