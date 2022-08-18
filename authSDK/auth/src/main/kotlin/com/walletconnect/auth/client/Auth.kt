package com.walletconnect.auth.client

import android.app.Application

object Auth {

    sealed interface Listeners {

    }

    sealed class Model {

        data class Error(val throwable: Throwable) : Model() // TODO: Should this be extracted to core for easier error handling?

        data class ConnectionState(val isAvailable: Boolean, ) : Model()

        data class AppMetaData(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<String>,
            val redirect: String?
        ) : Model()

        data class PendingRequest(
            val id: Long,
            val payloadParams: PayloadParams,
            val message: String
        ): Model() {

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
                val resources: List<String>?
            )
        }

        data class AuthRequest(
            val id: Long,
            val cacao: Cacao
        )

        data class AuthResponse(
            val id: Long,
            val message: String
        )

        sealed class Cacao: Model() {

            data class Header(val t: String): Cacao()

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
                val resources: List<String>?
            )

            data class Signature(val t: String, val s: String, val m: String?): Cacao()
        }
    }

    sealed class Params {

        data class Init(val application: Application, val appMetaData: Model.AppMetaData, val iss: String?) : Params()

        data class Pair(val uri: String): Params()

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
        ): Params()

        data class Respond(val id: Long, val signature: Model.Cacao.Signature): Params()

        data class RequestId(val id: Long)
    }
}