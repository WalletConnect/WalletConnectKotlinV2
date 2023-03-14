package com.walletconnect.web3.inbox.client

import androidx.annotation.Keep
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.chat.client.Chat


object Inbox {
    sealed class Params {
        data class Init(
            val core: CoreClient,
            val account: Type.AccountId,
            val onSign: (message: String) -> Model.Cacao.Signature,
            val keyServerUrl: String = Chat.DEFUALT_KEYSERVER_URL,
        ) : Params()
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

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
    }


    sealed interface Type {
        @JvmInline
        value class AccountId(val value: String) : Type //todo: Add common

    }
}