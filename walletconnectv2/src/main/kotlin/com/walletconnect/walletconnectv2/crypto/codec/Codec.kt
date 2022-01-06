package com.walletconnect.walletconnectv2.crypto.codec

import com.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import com.walletconnect.walletconnectv2.crypto.data.PublicKey
import com.walletconnect.walletconnectv2.crypto.data.SharedKey

interface Codec {
    fun encrypt(message: String, sharedKey: SharedKey, publicKey: PublicKey): String
    fun decrypt(payload: EncryptionPayload, sharedKey: SharedKey): String
}