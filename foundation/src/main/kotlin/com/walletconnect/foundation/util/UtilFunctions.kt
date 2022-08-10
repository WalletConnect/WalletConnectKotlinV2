@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.util

import com.sun.jndi.toolkit.url.Uri
import org.apache.http.client.utils.URIBuilder
import java.security.SecureRandom
import javax.ws.rs.core.UriBuilder

@JvmSynthetic
fun generateId(): Long = (System.currentTimeMillis() + (100..999).random())

@JvmSynthetic
fun randomBytes(size: Int): ByteArray = ByteArray(size).apply {
    SecureRandom().nextBytes(this)
}

@JvmSynthetic
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

@JvmSynthetic
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
internal fun String.addUserAgent(): String {
    return UriBuilder.fromUri(this)
        .queryParam("ua", """wc-2/kotlin-2.0.0-rc.1/android-integration-test""")
        .build()
        .toString()
}