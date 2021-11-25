@file:JvmName("Utils")

package org.walletconnect.walletconnectv2.util

import java.lang.System.currentTimeMillis

fun generateId(): Long = (currentTimeMillis() + (0..100).random())

fun ByteArray.bytesToHex(): String {
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