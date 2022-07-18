@file:JvmName("Utils")

package com.walletconnect.sign.util

import com.walletconnect.sign.core.model.utils.Time
import com.walletconnect.sign.core.model.vo.ExpiryVO
import java.lang.System.currentTimeMillis
import java.security.SecureRandom

@JvmSynthetic
internal fun ExpiryVO.isSequenceValid(): Boolean = seconds > Time.currentTimeInSeconds

@JvmSynthetic
internal fun randomBytes(size: Int): ByteArray =
    ByteArray(size).apply {
        SecureRandom().nextBytes(this)
    }

@JvmSynthetic
internal fun generateId(): Long = (currentTimeMillis() + (100..999).random())

@JvmSynthetic
internal fun ByteArray.bytesToHex(): String {
    val hexString = StringBuilder(2 * this.size)
    for (i in this.indices) {
        val hex = Integer.toHexString(0xff and this[i].toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}

@JvmSynthetic
internal fun String.hexToBytes(): ByteArray {
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

private fun getHexPrefix(input: String): String =
    if (containsHexPrefix(input)) {
        input.substring(2)
    } else {
        input
    }

private fun containsHexPrefix(input: String): Boolean = input.startsWith("0x")

@JvmSynthetic
internal fun Long.extractTimestamp() = this / 1000