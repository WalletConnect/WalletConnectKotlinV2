package com.walletconnect.android

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Namespace

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

        sealed class Namespace : Model() {

            //Required or Optional
            data class Proposal(
                val chains: List<String>? = null,
                val methods: List<String>,
                val events: List<String>,
            ) : Namespace()

            data class Session(
                val chains: List<String>? = null,
                val accounts: List<String>,
                val methods: List<String>,
                val events: List<String>,
            ) : Namespace()
        }

        sealed class Message {

            data class Simple(
                val title: String,
                val body: String
            ) : Message()

            data class Notify(
                val title: String,
                val body: String,
                val icon: String?,
                val url: String?,
                val type: String,
                val topic: String
            ) : Message()

            data class SessionProposal(
                val id: Long,
                val pairingTopic: String,
                val name: String,
                val description: String,
                val url: String,
                val icons: List<String>,
                val redirect: String,
                val requiredNamespaces: Map<String, Namespace.Proposal>,
                val optionalNamespaces: Map<String, Namespace.Proposal>,
                val properties: Map<String, String>?,
                val proposerPublicKey: String,
                val relayProtocol: String,
                val relayData: String?,
            ) : Message()

            data class SessionRequest(
                val topic: String,
                val chainId: String?,
                val peerMetaData: AppMetaData?,
                val request: JSONRPCRequest,
            ) : Message() {
                data class JSONRPCRequest(
                    val id: Long,
                    val method: String,
                    val params: String,
                ) : Message()
            }

            data class AuthRequest(
                val id: Long,
                val pairingTopic: String,
                val payloadParams: PayloadParams,
            ) : Message() {
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
    }
}