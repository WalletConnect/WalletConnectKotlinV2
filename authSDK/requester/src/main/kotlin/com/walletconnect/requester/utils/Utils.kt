package com.walletconnect.requester.utils

import kotlin.random.Random


fun randomNonce(): String = Random.nextBytes(16).bytesToHex()

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