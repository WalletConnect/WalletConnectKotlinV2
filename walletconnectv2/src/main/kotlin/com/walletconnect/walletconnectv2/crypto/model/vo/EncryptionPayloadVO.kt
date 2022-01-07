package com.walletconnect.walletconnectv2.crypto.model.vo

data class EncryptionPayloadVO(
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