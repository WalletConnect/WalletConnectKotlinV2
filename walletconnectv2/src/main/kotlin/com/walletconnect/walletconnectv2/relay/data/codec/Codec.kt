package com.walletconnect.walletconnectv2.relay.data.codec

import com.walletconnect.walletconnectv2.crypto.model.vo.EncryptionPayloadVO
import com.walletconnect.walletconnectv2.crypto.model.PublicKey
import com.walletconnect.walletconnectv2.crypto.model.SharedKey

interface Codec {
    fun encrypt(message: String, sharedKey: SharedKey, publicKey: PublicKey): String
    fun decrypt(payload: EncryptionPayloadVO, sharedKey: SharedKey): String
}