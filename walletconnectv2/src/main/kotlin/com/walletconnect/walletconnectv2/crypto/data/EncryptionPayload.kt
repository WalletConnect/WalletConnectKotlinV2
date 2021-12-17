package com.walletconnect.walletconnectv2.crypto.data

data class EncryptionPayload(
    val iv: String,
    val publicKey: String,
    val mac: String,
    val cipherText: String
) {
    companion object {
        const val ivLength = 32
        const val publicKeyLength = 64
        const val macLength = 64
    }
}