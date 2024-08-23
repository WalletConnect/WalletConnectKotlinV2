@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.util

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

fun ByteArray.bytesToInt(size: Int): Int {
    require(this.size <= 4) { "Byte array size must be less than 5" }

    return (0 until size).fold(0) { acc, i ->
        acc.or(this[i].toInt().shl((size - 1 - i) * 8))
    }
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
internal fun String.addUserAgent(sdkVersion: String): String = "$this&ua=wc-2/kotlin-$sdkVersion"