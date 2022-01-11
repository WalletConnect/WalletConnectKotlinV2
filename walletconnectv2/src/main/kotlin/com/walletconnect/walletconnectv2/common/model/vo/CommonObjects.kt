package com.walletconnect.walletconnectv2.common.model.vo

// TODO: Look into finding a way to convert these into value classes and still have Moshi deserialize them without adding property inside JSON object
data class TopicVO(val value: String)

data class TtlVO(val seconds: Long)

data class ExpiryVO(val seconds: Long)

data class SubscriptionIdVO(val id: String)

@JvmInline
value class PublicKey(override val keyAsHex: String) : Key

@JvmInline
value class PrivateKey(override val keyAsHex: String) : Key

@JvmInline
value class SharedKey(override val keyAsHex: String) : Key

interface Key {
    val keyAsHex: String
}