package com.walletconnect.android

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry

object Core {
    sealed interface Listeners {
        interface PairingPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

        sealed class Ping : Model() {
            data class Success(val topic: String) : Ping()
            data class Error(val error: Throwable) : Ping()
        }

        data class AppMetaData(val name: String, val description: String, val url: String, val icons: List<String>, val redirect: String?, val verifyUrl: String? = null) : Model()

        data class DeletedPairing(val topic: String, val reason: String) : Model()

        data class Pairing(
            val topic: String,
            val expiry: Long,
            val peerAppMetaData: AppMetaData? = null,
            val relayProtocol: String,
            val relayData: String?,
            val uri: String,
            val isActive: Boolean,
            val registeredMethods: String
        ) : Model()


        sealed class Message : Model() {
            data class Notify(
                val title: String,
                val body: String,
                val icon: String?,
                val url: String?,
                val type: String,
                val topic: String
            ) : Message()

            data class Simple(
                val title: String,
                val body: String
            ) : Message()

            data class Decrypted(
                val metadata: Metadata,
                val request: Request
            ) : Message() {
                data class Metadata(
                    val name: String,
                    val description: String,
                    val url: String,
                    val icons: List<String>,
                )

                data class Request(
                    val id: Long,
                    val method: String,
                    val params: String,
                )
            }
        }
    }

    sealed class Params {

        data class Ping(val topic: String) : Params()

        data class Pair(val uri: String) : Params()

        data class Disconnect(val topic: String) : Params()

        data class Activate(val topic: String) : Params()

        data class UpdateExpiry(val topic: String, val expiry: Expiry) : Params()

        data class UpdateMetadata(val topic: String, val metadata: Model.AppMetaData, val metaDataType: AppMetaDataType) : Params()

        data class DecryptMessage(val topic: String, val encryptedMessage: String) : Params()
    }
}