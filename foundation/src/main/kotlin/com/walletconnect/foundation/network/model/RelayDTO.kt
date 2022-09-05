package com.walletconnect.foundation.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.foundation.common.adapters.SubscriptionIdAdapter
import com.walletconnect.foundation.common.adapters.TopicAdapter
import com.walletconnect.foundation.common.adapters.TtlAdapter
import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl

sealed class RelayDTO {
    abstract val id: Long
    abstract val jsonrpc: String

    sealed class Publish : RelayDTO() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = IRN_PUBLISH,
            @Json(name = "params")
            val params: Params,
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
                val ttl: Ttl,
                @Json(name = "tag")
                val tag: Int,
                @Json(name = "prompt")
                val prompt: Boolean?,
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            val result: Boolean,
        ) : Publish()

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long,
        ) : Publish()
    }

    sealed class Subscribe : RelayDTO() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = IRN_SUBSCRIBE,
            @Json(name = "params")
            val params: Params,
        ) : Subscribe() {

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "topic")
                @field:TopicAdapter.Qualifier
                val topic: Topic,
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            @field:SubscriptionIdAdapter.Qualifier
            val result: SubscriptionId,
        ) : Subscribe()

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long,
        ) : Subscribe()
    }

    sealed class Subscription : RelayDTO() {

        @JsonClass(generateAdapter = true)
        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = IRN_SUBSCRIPTION,
            @Json(name = "params")
            val params: Params,
        ) : Subscription() {

            val subscriptionTopic: Topic = params.subscriptionData.topic
            val message: String = params.subscriptionData.message

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "id")
                @field:SubscriptionIdAdapter.Qualifier
                val subscriptionId: SubscriptionId,
                @Json(name = "data")
                val subscriptionData: SubscriptionData,
            ) {

                @JsonClass(generateAdapter = true)
                data class SubscriptionData(
                    @Json(name = "topic")
                    @field:TopicAdapter.Qualifier
                    val topic: Topic,
                    @Json(name = "message")
                    val message: String, //ack, jsonrpc error, eth_sign
                )
            }
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            val result: Boolean,
        ) : Subscription()

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long,
        ) : Subscription()
    }

    sealed class Unsubscribe : RelayDTO() {

        data class Request(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            val method: String = IRN_UNSUBSCRIBE,
            @Json(name = "params")
            val params: Params,
        ) : Unsubscribe() {

            data class Params(
                @Json(name = "topic")
                @field:TopicAdapter.Qualifier
                val topic: Topic,
                @Json(name = "id")
                @field:SubscriptionIdAdapter.Qualifier
                val subscriptionId: SubscriptionId,
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            val result: Boolean,
        ) : Unsubscribe()

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long,
        ) : Unsubscribe()
    }

    data class Error(
        @Json(name = "code")
        val code: Long,
        @Json(name = "message")
        val message: String,
    ) {
        val errorMessage: String = "Error code: $code; Error message: $message"
    }
}