package org.walletconnect.walletconnectv2.engine.serailising

import org.walletconnect.walletconnectv2.moshi

inline fun <reified T> trySerialize(type: T): String =
    moshi.adapter(T::class.java).toJson(type)

inline fun <reified T> tryDeserialize(json: String): T? {
    return runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
}