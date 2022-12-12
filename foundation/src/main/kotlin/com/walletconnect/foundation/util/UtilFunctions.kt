@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.util

import jakarta.ws.rs.core.UriBuilder
import java.security.SecureRandom

@get:JvmSynthetic
val String.Companion.Empty
    get() = ""

fun generateId(): Long = (System.currentTimeMillis() + (100..999).random())

fun randomBytes(size: Int): ByteArray = ByteArray(size).apply {
    SecureRandom().nextBytes(this)
}

fun ByteArray.bytesToHex(): String {
    val hexString = StringBuilder(2 * this.size)

    this.indices.forEach { i ->
        val hex = Integer.toHexString(0xff and this[i].toInt())

        if (hex.length == 1) {
            hexString.append('0')
        }

        hexString.append(hex)
    }

    return hexString.toString()
}

fun String.hexToBytes(): ByteArray {
    val len = this.length
    val data = ByteArray(len / 2)
    var i = 0

    while (i < len) {
        data[i / 2] = ((Character.digit(this[i], 16) shl 4)
                + Character.digit(this[i + 1], 16)).toByte()
        i += 2
    }

    return data
}

@JvmSynthetic
internal fun String.addUserAgent(sdkVersion: String): String {
    return UriBuilder.fromUri(this)
        .queryParam("ua", """wc-2/kotlin-$sdkVersion""")
        .build()
        .toString()
}