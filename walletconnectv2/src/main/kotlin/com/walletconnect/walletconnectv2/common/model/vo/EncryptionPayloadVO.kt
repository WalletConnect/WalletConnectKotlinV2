package com.walletconnect.walletconnectv2.common.model.vo

internal data class EncryptionPayloadVO(
    val iv: String,
    val publicKey: String,
    val mac: String,
    val cipherText: String
) {
    internal companion object {
        const val ivLength = 32
        const val publicKeyLength = 64
        const val macLength = 64
    }
}