@file:JvmName("Utils")

package org.walletconnect.walletconnectv2.util

import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
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