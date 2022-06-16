package com.walletconnect.sign.core.model.vo

// TODO: Look into finding a way to convert these into value classes and still have Moshi deserialize them without adding property inside JSON object
internal data class TopicVO(val value: String)

internal data class TtlVO(val seconds: Long)

internal data class ExpiryVO(val seconds: Long)

internal data class SubscriptionIdVO(val id: String)

@JvmInline
internal value class SecretKey(override val keyAsHex: String) : Key

@JvmInline
internal value class PublicKey(override val keyAsHex: String) : Key

@JvmInline
internal value class PrivateKey(override val keyAsHex: String) : Key

internal interface Key {
    val keyAsHex: String
}