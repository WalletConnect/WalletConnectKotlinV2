package com.walletconnect.walletconnectv2.relay.data

import com.walletconnect.walletconnectv2.crypto.model.EncryptionPayload
import com.walletconnect.walletconnectv2.crypto.model.PublicKey
import com.walletconnect.walletconnectv2.crypto.model.SharedKey

interface Codec {
    fun encrypt(message: String, sharedKey: SharedKey, publicKey: PublicKey): String
    fun decrypt(payload: EncryptionPayload, sharedKey: SharedKey): String
}