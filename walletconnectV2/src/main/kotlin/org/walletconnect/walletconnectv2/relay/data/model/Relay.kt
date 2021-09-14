package org.walletconnect.walletconnectv2.relay.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl

// TODO: Maybe look into separating children into different files
sealed class Relay {
    abstract val id: Int
    abstract val jsonrpc: String

    sealed class Publish: Relay() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_publish",
            @Json(name = "params")
            val params: Params
        ): Publish() {

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "topic")
                val topic: Topic,
                @Json(name = "message")
                val message: String,
                @Json(name = "ttl")
                val ttl: Ttl = Ttl(86400)
            )
        }

        class Response(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
        ): Publish()
    }

    sealed class Subscribe: Relay() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_subscribe",
            @Json(name = "params")
            val params: Params
        ): Subscribe() {

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "topic")
                val topic: Topic,
            )
        }

        data class Response(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
        ): Subscribe()
    }

    sealed class Subscription: Relay() {

        @JsonClass(generateAdapter = true)
        data class Response(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = "waku_subscription",
            @Json(name = "params")
            val params: Params
        ): Subscription() {

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "id")
                val subscriptionId: Int,
                @Json(name = "data")
                val data: SubscriptionData
            ) {

                @JsonClass(generateAdapter = true)
                data class SubscriptionData(
                    @Json(name = "topic")
                    val topic: Topic,
                    @Json(name = "message")
                    val message: String
                )
            }
        }

        data class Request(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
        ): Subscription()
    }

    sealed class Unsubscribe: Relay() {

        data class Request(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
        ): Unsubscribe()

        data class Response(
            @Json(name = "id")
            override val id: Int,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
        ): Unsubscribe()
    }
}