package com.walletconnect.web3.inbox.client

import androidx.annotation.Keep
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface


object Inbox {

    sealed class Params {
        data class Init(
            val core: CoreInterface,
            val account: Type.AccountId,
            val onSign: (message: String) -> Model.Cacao.Signature,
            val config: Model.Config = Model.Config(),
        ) : Params()
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

        data class Config(
            val isChatEnabled: Boolean = false,
            val isNotifyEnabled: Boolean = true,
            val areSettingsEnabled: Boolean = false,
        ) : Model()

        sealed class Events : Model() {
            data class OnSign(val message: String) : Events()
        }

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

        sealed class Message : Model() {
            abstract val title: String
            abstract val body: String

            data class Simple(
                override val title: String,
                override val body: String,
            ) : Message()

            data class Decrypted(
                override val title: String,
                override val body: String,
                val icon: String?,
                val url: String?,
                val type: String?,
            ) : Message()
        }
    }

    sealed interface Type {
        @JvmInline
        value class AccountId(val value: String) : Type //todo: Add common
    }
}