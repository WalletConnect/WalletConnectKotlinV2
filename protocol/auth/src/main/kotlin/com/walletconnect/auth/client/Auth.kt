package com.walletconnect.auth.client

import androidx.annotation.Keep
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.signing.cacao.Issuer

object Auth {

    sealed class Event {
        data class AuthRequest(
            val id: Long,
            val pairingTopic: String,
            val payloadParams: Model.PayloadParams,
        ) : Event()

        data class AuthResponse(val response: Model.Response) : Event()

        data class VerifyContext(
            val id: Long,
            val origin: String,
            val validation: Model.Validation,
            val verifyUrl: String,
            val isScam: Boolean?
        ) : Event()

        data class ConnectionStateChange(
            val state: Model.ConnectionState,
        ) : Event()

        data class Error(
            val error: Model.Error,
        ) : Event()
    }

    sealed class Model {

        data class Error(val throwable: Throwable) : Model()

        data class ConnectionState(val isAvailable: Boolean) : Model()

        enum class Validation {
            VALID, INVALID, UNKNOWN
        }

        data class PendingRequest(
            val id: Long,
            val pairingTopic: String,
            val payloadParams: PayloadParams,
        ) : Model()

        data class VerifyContext(
            val id: Long,
            val origin: String,
            val validation: Validation,
            val verifyUrl: String,
            val isScam: Boolean?
        ) : Event()

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
                val address: String
                    get() = Issuer(iss).address
            }
        }

        sealed class Response : Model() {
            abstract val id: Long

            data class Result(override val id: Long, val cacao: Cacao) : Response()
            data class Error(override val id: Long, val code: Int, val message: String) : Response()
        }
    }

    sealed class Params {

        data class Init(val core: CoreInterface) : Params()

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
            val expiry: Long? = null,
        ) : Params()

        sealed class Respond : Params() {
            abstract val id: Long

            data class Result(override val id: Long, val signature: Model.Cacao.Signature, val issuer: String) : Respond()
            data class Error(override val id: Long, val code: Int, val message: String) : Respond()
        }

        data class FormatMessage(val payloadParams: Model.PayloadParams, val issuer: String) : Params()
    }
}