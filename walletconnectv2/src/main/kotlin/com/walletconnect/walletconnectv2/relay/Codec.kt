package com.walletconnect.walletconnectv2.relay

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.payload.EncryptionPayloadVO

internal interface Codec {
    fun encrypt(message: String, key: Key, publicKey: PublicKey): String
    fun decrypt(payload: EncryptionPayloadVO, key: Key): String
}