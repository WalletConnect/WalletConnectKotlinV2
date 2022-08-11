package com.walletconnect.foundation.network.model

object Relay {
    sealed class Model {
        sealed class Call : Relay.Model() {
            abstract val id: Long
            abstract val jsonrpc: String

            sealed class Publish : Call() {

                data class Request(
                    override val id: Long,
                    override val jsonrpc: String = "2.0",
                    val method: String = IRN_PUBLISH,
                    val params: Params,
                ) : Publish() {

                    data class Params(
                        val topic: String,
                        val message: String,
                        val ttl: Long,
                        val tag: Int,
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
                    val error: Error,
                    override val id: Long,
                ) : Publish()
            }

            sealed class Subscribe : Call() {

                data class Request(
                    override val id: Long,
                    override val jsonrpc: String = "2.0",
                    val method: String = IRN_SUBSCRIBE,
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
                    val error: Error,
                    override val id: Long,
                ) : Subscribe()
            }

            sealed class Subscription : Call() {

                data class Request(
                    override val id: Long,
                    override val jsonrpc: String = "2.0",
                    val method: String = IRN_SUBSCRIPTION,
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
                    val error: Error,
                    override val id: Long,
                ) : Subscription()
            }

            sealed class Unsubscribe : Call() {

                data class Request(
                    override val id: Long,
                    override val jsonrpc: String = "2.0",
                    val method: String = IRN_UNSUBSCRIBE,
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
                    val error: Error,
                    override val id: Long,
                ) : Unsubscribe()
            }
        }

        data class Error(
            val code: Long,
            val message: String,
        ) : Relay.Model() {
            val errorMessage: String = "Error code: $code; Error message: $message"
        }

        sealed class Event : Relay.Model() {
            data class OnConnectionOpened<out WEB_SOCKET : Any>(val webSocket: WEB_SOCKET) : Event()
            data class OnMessageReceived(val message: Message) : Event()
            data class OnConnectionClosing(val shutdownReason: ShutdownReason) : Event()
            data class OnConnectionClosed(val shutdownReason: ShutdownReason) : Event()
            data class OnConnectionFailed(val throwable: Throwable) : Event()
        }

        sealed class Message : Relay.Model() {
            data class Text(val value: String) : Message()
            class Bytes(val value: ByteArray) : Message()
        }

        data class ShutdownReason(val code: Int, val reason: String) : Relay.Model()

        data class IrnParams(val tag: Int, val ttl: Long, val prompt: Boolean = false) : Relay.Model()
    }
}