package org.walletconnect.walletconnectv2.crypto

import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey

interface Codec {
    fun encrypt(message: String, sharedKey: SharedKey, publicKey: PublicKey): String
    fun decrypt(payload: EncryptionPayload, sharedKey: SharedKey): String
}