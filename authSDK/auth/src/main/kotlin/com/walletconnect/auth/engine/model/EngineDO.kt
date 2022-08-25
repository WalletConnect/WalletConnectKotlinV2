@file:JvmSynthetic

package com.walletconnect.auth.engine.model

import com.walletconnect.android_core.common.model.SymmetricKey
import com.walletconnect.android_core.common.model.type.EngineEvent
import com.walletconnect.auth.client.Auth
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android_core.common.model.RelayProtocolOptions as CoreRelayProtocolOptions

internal sealed class EngineDO {

    internal sealed class Events : EngineEvent, EngineDO() {
        data class onAuthRequest(val id: Long, val message: String) : Events()
        data class onAuthResponse(val id: Long, val response: AuthResponse) : Events()
    }

    internal sealed class AuthResponse : EngineDO() {
        data class Result(val cacao: Cacao) : AuthResponse()
        data class Error(val code: Int, val message: String) : AuthResponse()
    }

    internal sealed class Respond : EngineDO() {
        abstract val id: Long

        data class Result(override val id: Long, val signature: Auth.Model.Cacao.Signature) : Respond()
        data class Error(override val id: Long, val code: Int, val message: String) : Respond()
    }

    sealed class Response : EngineDO() {
        abstract val id: Long

        data class Result(override val id: Long, val cacao: Cacao) : Response()
        data class Error(override val id: Long, val code: Int, val message: String) : Response()
    }

    internal class WalletConnectUri(
        val topic: Topic,
        val symKey: SymmetricKey,
        val relay: CoreRelayProtocolOptions,
        val version: String = "2",
    ) : EngineDO()

    @JvmInline
    internal value class Issuer(val value: String)

    internal data class AppMetaData(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>,
        val redirect: String?,
    ) : EngineDO()

    internal data class Pairing(val uri: String) : EngineDO()

    internal data class PayloadParams(
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
    ) : EngineDO()

    internal data class Cacao(
        val header: Header,
        val payload: Payload,
        val signature: Signature,
    ) : EngineDO() {
        data class Signature(val t: String, val s: String, val m: String? = null) : EngineDO()
        data class Header(val t: String) : EngineDO()
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
        ) : EngineDO() {
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

    internal data class PendingRequest(
        val id: Long,
        val payloadParams: PayloadParams,
        val message: String,
    ) : EngineDO()
}