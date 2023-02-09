package com.walletconnect.android.internal.common.crypto

import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import java.security.MessageDigest


private const val SHA_256: String = "SHA-256"

fun sha256(key: String): String {
    val messageDigest: MessageDigest = MessageDigest.getInstance(SHA_256)
    val hashedBytes: ByteArray = messageDigest.digest(key.hexToBytes())

    return hashedBytes.bytesToHex()
}