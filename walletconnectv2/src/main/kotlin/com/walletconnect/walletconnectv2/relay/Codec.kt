package com.walletconnect.walletconnectv2.relay

import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SharedKey
import com.walletconnect.walletconnectv2.core.model.vo.payload.EncryptionPayloadVO

internal interface Codec {
    fun encrypt(message: String, sharedKey: SharedKey, publicKey: PublicKey): String
    fun decrypt(payload: EncryptionPayloadVO, sharedKey: SharedKey): String
}