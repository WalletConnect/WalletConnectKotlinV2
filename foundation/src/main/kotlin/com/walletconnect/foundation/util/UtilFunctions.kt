@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.util

import io.ipfs.multibase.Base16
import jakarta.ws.rs.core.UriBuilder
import java.security.SecureRandom
import kotlin.math.pow

@get:JvmSynthetic
val String.Companion.Empty
    get() = ""

fun generateId(): Long = ("${System.currentTimeMillis()}${(100..999).random()}").toLong()

fun generateClientToServerId(): Long {
    val now = System.currentTimeMillis() * 10.0.pow(6.0)
    return now.plus((100000..999999).random()).toLong()
}

fun randomBytes(size: Int): ByteArray = ByteArray(size).apply {
    SecureRandom().nextBytes(this)
}

fun ByteArray.bytesToHex(): String = Base16.encode(this)

fun String.hexToBytes(): ByteArray = Base16.decode(this.lowercase())

fun ByteArray.bytesToInt(size: Int): Int {
    require(this.size <= 4) { "Byte array size must be less than 5" }

    return (0 until size).fold(0) { acc, i ->
        acc.or(this[i].toInt().shl((size - 1 - i) * 8))
    }
}

@JvmSynthetic
internal fun String.addUserAgent(sdkVersion: String): String {
    return UriBuilder.fromUri(this)
        .queryParam("ua", """wc-2/kotlin-$sdkVersion""")
        .build()
        .toString()
}