package org.walletconnect.walletconnectv2.engine.serailising

import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.moshi


inline fun <reified T> trySerialize(type: T): String =
    moshi.adapter(T::class.java).toJson(type)

inline fun <reified T> tryDeserialize(json: String): T? {
    return runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
}

fun String.toEncryptionPayload(): EncryptionPayload {
    val pubKeyStartIndex = EncryptionPayload.ivLength
    val macStartIndex = pubKeyStartIndex + EncryptionPayload.publicKeyLength
    val cipherTextStartIndex = macStartIndex + EncryptionPayload.macLength

    val iv = this.substring(0, pubKeyStartIndex)
    val publicKey = this.substring(pubKeyStartIndex, macStartIndex)
    val mac = this.substring(macStartIndex, cipherTextStartIndex)
    val cipherText = this.substring(cipherTextStartIndex, this.length)

    return EncryptionPayload(iv, publicKey, mac, cipherText)
}

fun String.encode(): String = this.encodeToByteArray().joinToString(separator = "") { bytes -> String.format("%02X", bytes) }