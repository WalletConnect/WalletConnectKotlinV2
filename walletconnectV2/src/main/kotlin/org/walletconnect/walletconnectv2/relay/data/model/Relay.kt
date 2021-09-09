package org.walletconnect.walletconnectv2.relay.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl

// TODO: Maybe look into separating children into different files
sealed class Relay {

    sealed class Publish: Relay() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            val id: Int,
            @Json(name = "jsonrpc")
            val jsonrpc: String = "2.0",
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

        class Response(): Publish()
    }

    sealed class Subscribe: Relay() {

        class Request: Subscribe()

        class Response: Subscribe()
    }

    sealed class Subscription: Relay() {

        class Response(): Subscription()

        class Request(): Subscription()
    }

    sealed class Unsubscribe: Relay() {

        class Request(): Unsubscribe()

        class Response(): Unsubscribe()
    }
}