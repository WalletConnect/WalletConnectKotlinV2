package com.walletconnect.walletconnectv2.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.adapters.SubscriptionIdAdapter
import com.walletconnect.walletconnectv2.common.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.common.adapters.TtlAdapter

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
                val topic: TopicVO,
                @Json(name = "message")
                val message: String,
                @Json(name = "ttl")
                @field:TtlAdapter.Qualifier
                val ttl: TtlVO = TtlVO(86400)
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

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long
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
                val topic: TopicVO
            )
        }

        data class Acknowledgement(
            @Json(name = "id")
            override val id: Long,
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "result")
            @field:SubscriptionIdAdapter.Qualifier
            val result: SubscriptionIdVO
        ) : Subscribe()

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long
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

            val subscriptionTopic: TopicVO = params.subscriptionData.topic
            val message: String = params.subscriptionData.message

            @JsonClass(generateAdapter = true)
            data class Params(
                @Json(name = "id")
                @field:SubscriptionIdAdapter.Qualifier
                val subscriptionId: SubscriptionIdVO,
                @Json(name = "data")
                val subscriptionData: SubscriptionData
            ) {

                @JsonClass(generateAdapter = true)
                data class SubscriptionData(
                    @Json(name = "topic")
                    @field:TopicAdapter.Qualifier
                    val topic: TopicVO,
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

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long
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
                val topic: TopicVO,
                @Json(name = "id")
                @field:SubscriptionIdAdapter.Qualifier
                val subscriptionId: SubscriptionIdVO
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

        data class JsonRpcError(
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "error")
            val error: Error,
            @Json(name = "id")
            override val id: Long
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