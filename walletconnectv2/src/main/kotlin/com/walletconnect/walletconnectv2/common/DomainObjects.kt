package com.walletconnect.walletconnectv2.common

// TODO: Look into finding a way to convert these into value classes and still have Moshi deserialize them without adding property inside JSON object
data class Topic(val value: String)

data class Ttl(val seconds: Long)

data class Expiry(val seconds: Long)

data class SubscriptionId(val id: String)