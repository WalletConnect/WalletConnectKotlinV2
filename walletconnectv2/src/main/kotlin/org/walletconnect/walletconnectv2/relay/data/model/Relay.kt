package org.walletconnect.walletconnectv2.relay.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.common.network.adapters.SubscriptionIdAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter
import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.util.toEncryptionPayload

// TODO: Maybe look into separating children into different files
sealed class Relay {
    abstract val id: Long
    abstract val jsonrpc: String

    sealed class Publish : Relay() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_publish",
            @Json(name = "params")
            val params: Params
        ) : Publish() {

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "topic")
                @field:TopicAdapter.Qualifier
                val topic: Topic,
                @Json(name = "message")
                val message: String,
                @Json(name = "ttl")
                @field:TtlAdapter.Qualifier
                val ttl: Ttl = Ttl(86400)
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            val result: Boolean
        ) : Publish()
    }

    sealed class Subscribe : Relay() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_subscribe",
            @Json(name = "params")
            val params: Params
        ) : Subscribe() {

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "topic")
                @field:TopicAdapter.Qualifier
                val topic: Topic
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            @field:SubscriptionIdAdapter.Qualifier
            val result: SubscriptionId
        ) : Subscribe()
    }

    sealed class Subscription : Relay() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_subscription",
            @Json(name = "params")
            val params: Params
        ) : Subscription() {

            val subscriptionTopic: Topic = params.subscriptionData.topic
            val encryptionPayload: EncryptionPayload =
                params.subscriptionData.message.toEncryptionPayload()

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "id")
                @field:SubscriptionIdAdapter.Qualifier
                val subscriptionId: SubscriptionId,
                @Json(name = "data")
                val subscriptionData: SubscriptionData
            ) {

                @JsonClass(generateAdapter = true)
                data class SubscriptionData(
                    @Json(name = "topic")
                    @field:TopicAdapter.Qualifier
                    val topic: Topic,
                    @Json(name = "message")
                    val message: String
                )
            }
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            val result: Boolean
        ) : Subscription()
    }

    sealed class Unsubscribe : Relay() {

        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_unsubscribe",
            @Json(name = "params")
            val params: Params
        ) : Unsubscribe() {

            data class Params(
                @Json(name = "topic")
                @field:TopicAdapter.Qualifier
                val topic: Topic,
                @Json(name = "id")
                @field:SubscriptionIdAdapter.Qualifier
                val subscriptionId: SubscriptionId
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            val result: Boolean
        ) : Unsubscribe()
    }
}