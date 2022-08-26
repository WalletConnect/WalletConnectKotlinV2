@file:JvmSynthetic

package com.walletconnect.auth.engine.model

import com.walletconnect.android_core.common.model.SymmetricKey
import com.walletconnect.android_core.common.model.type.EngineEvent
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.common.model.CacaoVO
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android_core.common.model.RelayProtocolOptions as CoreRelayProtocolOptions

internal sealed class EngineDO {

    internal sealed class Events : EngineEvent, EngineDO() {
        data class onAuthRequest(val id: Long, val message: String) : Events()
        data class onAuthResponse(val id: Long, val response: AuthResponse) : Events()
    }

    internal sealed class AuthResponse : EngineDO() {
        data class Result(val cacao: CacaoVO) : AuthResponse()
        data class Error(val code: Int, val message: String) : AuthResponse()
    }

    internal sealed class Respond : EngineDO() {
        abstract val id: Long

        data class Result(override val id: Long, val signature: Auth.Model.Cacao.Signature) : Respond()
        data class Error(override val id: Long, val code: Int, val message: String) : Respond()
    }

    internal class WalletConnectUri(
        val topic: Topic,
        val symKey: SymmetricKey,
        val relay: CoreRelayProtocolOptions,
        val version: String = "2",
    ) : EngineDO()

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
}