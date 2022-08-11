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
                val cacao: Cacao,
            )

            data class AuthResponse(
                val id: Long,
                val message: String,
            )
        }


        data class Cacao(
            val header: CacaoHeader,
            val payload: CacaoPayload,
            val signature: CacaoSignature,
        )

        data class CacaoSignature(val t: String, val s: String, val m: String? = null)
        data class CacaoHeader(val t: String)

        data class CacaoPayload(
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
        ) {
            val address: String
            val chainId: String

            init {
                iss.split(ISS_DELIMITER).apply {
                    address = get(ISS_POSITION_OF_ADDRESS)
                    chainId = get(ISS_POSITION_OF_CHAIN_ID)
                }
            }

            companion object {
                private const val ISS_DELIMITER = ":"
                private const val ISS_POSITION_OF_CHAIN_ID = 3
                private const val ISS_POSITION_OF_ADDRESS = 4
            }
        }


        sealed class Response : Model() {
            data class Cacao(val cacao: Cacao) : Response()
            data class ErrorResponse(val code: Int, val message: String) : Response()
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
            data class ResultResponse(val id: Long, val signature: Model.CacaoSignature) : Respond()
            data class ErrorResponse(val code: Int, val message: String) : Respond()
        }

        data class RequestId(val id: Long)
    }
}