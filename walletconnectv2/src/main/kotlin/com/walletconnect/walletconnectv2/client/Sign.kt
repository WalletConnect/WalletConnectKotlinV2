package com.walletconnect.walletconnectv2.client

import android.app.Application
import android.net.Uri
import com.walletconnect.walletconnectv2.network.Relay
import java.net.URI

object Sign {

    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    enum class ConnectionType {
        AUTOMATIC, MANUAL
    }

    sealed class Model {

        data class Error(val throwable: Throwable) : Model()

        sealed class ProposedSequence {
            class Pairing(val uri: String) : ProposedSequence()
            object Session : ProposedSequence()
        }

        data class SessionProposal(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<URI>,
            val requiredNamespaces: Map<String, Namespace.Proposal>,
            val proposerPublicKey: String,
            val relayProtocol: String,
            val relayData: String?,
        ) : Model()

        data class SessionRequest(
            val topic: String,
            val chainId: String?,
            val peerMetaData: AppMetaData?,
            val request: JSONRPCRequest,
        ) : Model() {

            data class JSONRPCRequest(
                val id: Long,
                val method: String,
                val params: String,
            ) : Model()
        }

        sealed class Namespace: Model() {

            data class Proposal(val chains: List<String>, val methods: List<String>, val events: List<String>, val extensions: List<Extension>?): Namespace() {

                data class Extension(val chains: List<String>, val methods: List<String>, val events: List<String>)
            }

            data class Session(val accounts: List<String>, val methods: List<String>, val events: List<String>, val extensions: List<Extension>?): Namespace() {

                data class Extension(val accounts: List<String>, val methods: List<String>, val events: List<String>)
            }
        }

        data class RelayProtocolOptions(val protocol: String, val data: String? = null) : Model()

        data class Pairing(val topic: String, val metaData: AppMetaData?) : Model()

        sealed class SettledSessionResponse : Model() {
            data class Result(val session: Session) : SettledSessionResponse()
            data class Error(val errorMessage: String) : SettledSessionResponse()
        }

        sealed class SessionUpdateResponse : Model() {
            data class Result(val topic: String, val namespaces: Map<String, Namespace.Session>) : SessionUpdateResponse()
            data class Error(val errorMessage: String) : SessionUpdateResponse()
        }

        sealed class DeletedSession : Model() {
            data class Success(val topic: String, val reason: String) : DeletedSession()
            data class Error(val error: Throwable) : DeletedSession()
        }

        sealed class Ping : Model() {
            data class Success(val topic: String) : Ping()
            data class Error(val error: Throwable) : Ping()
        }

        data class RejectedSession(val topic: String, val reason: String) : Model()

        data class Blockchain(val chains: List<String>) : Model()

        data class UpdatedSession(val topic: String, val namespaces: Map<String, Namespace.Session>) : Model()

        data class ApprovedSession(
            val topic: String,
            val metaData: AppMetaData?,
            val namespaces: Map<String, Namespace.Session>,
            val accounts: List<String>,
        ) : Model()

        data class Session(
            val topic: String,
            val expiry: Long,
            val namespaces: Map<String, Namespace.Session>,
            val metaData: AppMetaData?,
        ) : Model()

        data class SessionEvent(
            val name: String,
            val data: String,
        ) : Model()

        data class SessionRequestResponse(
            val topic: String,
            val chainId: String?,
            val method: String,
            val result: JsonRpcResponse,
        ) : Model()

        sealed class JsonRpcResponse : Model() {
            abstract val id: Long
            val jsonrpc: String = "2.0"

            data class JsonRpcResult(
                override val id: Long,
                val result: String,
            ) : JsonRpcResponse()

            data class JsonRpcError(
                override val id: Long,
                val code: Int,
                val message: String,
            ) : JsonRpcResponse()
        }

        data class AppMetaData(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<String>,
        ) : Model()

        data class PendingRequest(
            val requestId: Long,
            val topic: String,
            val method: String,
            val chainId: String?,
            val params: String,
        ) : Model()

        data class ConnectionState(
            val isAvailable: Boolean
        ) : Model()

        sealed class Relay : Model() {
            sealed class Call : Relay() {
                abstract val id: Long
                abstract val jsonrpc: String

                sealed class Publish : Call() {

                    data class Request(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val method: String = "waku_publish",
                        val params: Params,
                    ) : Publish() {

                        data class Params(
                            val topic: String,
                            val message: String,
                            val ttl: Long,
                            val prompt: Boolean?,
                        )
                    }

                    data class Acknowledgement(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val result: Boolean,
                    ) : Publish()

                    data class JsonRpcError(
                        override val jsonrpc: String = "2.0",
                        val error: Sign.Model.Relay.Error,
                        override val id: Long,
                    ) : Publish()
                }

                sealed class Subscribe : Call() {

                    data class Request(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val method: String = "waku_subscribe",
                        val params: Params,
                    ) : Subscribe() {

                        data class Params(
                            val topic: String,
                        )
                    }

                    data class Acknowledgement(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val result: String,
                    ) : Subscribe()

                    data class JsonRpcError(
                        override val jsonrpc: String = "2.0",
                        val error: Sign.Model.Relay.Error,
                        override val id: Long,
                    ) : Subscribe()
                }

                sealed class Subscription : Call() {

                    data class Request(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val method: String = "waku_subscription",
                        val params: Params,
                    ) : Subscription() {

                        val subscriptionTopic: String = params.subscriptionData.topic
                        val message: String = params.subscriptionData.message

                        data class Params(
                            val subscriptionId: String,
                            val subscriptionData: SubscriptionData,
                        ) {

                            data class SubscriptionData(
                                val topic: String,
                                val message: String,
                            )
                        }
                    }

                    data class Acknowledgement(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val result: Boolean,
                    ) : Subscription()

                    data class JsonRpcError(
                        override val jsonrpc: String = "2.0",
                        val error: Sign.Model.Relay.Error,
                        override val id: Long,
                    ) : Subscription()
                }

                sealed class Unsubscribe : Call() {

                    data class Request(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val method: String = "waku_unsubscribe",
                        val params: Params,
                    ) : Unsubscribe() {

                        data class Params(
                            val topic: String,
                            val subscriptionId: String,
                        )
                    }

                    data class Acknowledgement(
                        override val id: Long,
                        override val jsonrpc: String = "2.0",
                        val result: Boolean,
                    ) : Unsubscribe()

                    data class JsonRpcError(
                        override val jsonrpc: String = "2.0",
                        val error: Sign.Model.Relay.Error,
                        override val id: Long,
                    ) : Unsubscribe()
                }
            }

            data class Error(
                val code: Long,
                val message: String,
            ) : Relay() {
                val errorMessage: String = "Error code: $code; Error message: $message"
            }

            sealed class Event : Relay() {
                data class OnConnectionOpened<out WEB_SOCKET : Any>(val webSocket: WEB_SOCKET) :
                    Event()

                data class OnMessageReceived(val message: Message) : Event()
                data class OnConnectionClosing(val shutdownReason: ShutdownReason) : Event()
                data class OnConnectionClosed(val shutdownReason: ShutdownReason) : Event()
                data class OnConnectionFailed(val throwable: Throwable) : Event()
            }

            sealed class Message : Relay() {
                data class Text(val value: String) : Message()
                class Bytes(val value: ByteArray) : Message()
            }

            data class ShutdownReason(val code: Int, val reason: String) : Relay()
        }
    }

    sealed class Params {

        // TODO: Maybe convert this into a Builder
        data class Init internal constructor(
            val application: Application,
            val metadata: Model.AppMetaData,
            val relay: Relay? = null,
            val connectionType: ConnectionType,
        ) : Params() {
            internal lateinit var serverUrl: String

            constructor(
                application: Application,
                useTls: Boolean,
                hostName: String,
                projectId: String,
                metadata: Model.AppMetaData,
                relay: Relay? = null,
                connectionType: ConnectionType = ConnectionType.AUTOMATIC,
            ) : this(application, metadata, relay, connectionType) {
                val relayServerUrl = Uri.Builder().scheme((if (useTls) "wss" else "ws"))
                    .authority(hostName)
                    .appendQueryParameter("projectId", projectId)
                    .build()
                    .toString()

                require(relayServerUrl.isValidRelayServerUrl()) {
                    "Check the schema and projectId parameter of the Server Url"
                }

                this.serverUrl = relayServerUrl
            }

            constructor(
                application: Application,
                relayServerUrl: String,
                metadata: Model.AppMetaData,
                relay: Relay? = null,
                connectionType: ConnectionType = ConnectionType.AUTOMATIC,
            ) : this(application, metadata, relay, connectionType) {
                require(relayServerUrl.isValidRelayServerUrl()) {
                    "Check the schema and projectId parameter of the Server Url"
                }

                this.serverUrl = relayServerUrl
            }

            private fun String.isValidRelayServerUrl(): Boolean {
                return this.isNotBlank() && Uri.parse(this)?.let { relayUrl ->
                    arrayOf("wss", "ws").contains(relayUrl.scheme) && !relayUrl.getQueryParameter("projectId").isNullOrBlank()
                } ?: false
            }
        }

        data class Connect(
            val namespaces: Map<String, Model.Namespace.Proposal>,
            val relays: List<Model.RelayProtocolOptions>? = null,
            val pairingTopic: String? = null,
        ) : Params()

        data class Pair(val uri: String) : Params()

        data class Approve(
            val proposerPublicKey: String,
            val namespaces: Map<String, Model.Namespace.Session>,
            val relayProtocol: String? = null,
        ) : Params()

        data class Reject(val proposerPublicKey: String, val reason: String, val code: Int) : Params()

        data class Disconnect(val sessionTopic: String, val reason: String, val reasonCode: Int) : Params()

        data class Response(val sessionTopic: String, val jsonRpcResponse: Model.JsonRpcResponse) : Params()

        data class Request(val sessionTopic: String, val method: String, val params: String, val chainId: String) : Params()

        data class Update(
            val sessionTopic: String,
            val namespaces: Map<String, Model.Namespace.Session>,
        ) : Params()

        data class Ping(val topic: String) : Params()

        data class Emit(val topic: String, val event: Model.SessionEvent, val chainId: String) : Params()

        data class Extend(val topic: String) : Params()
    }
}