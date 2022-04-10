package com.walletconnect.walletconnectv2.relay

import com.walletconnect.walletconnectv2.core.model.vo.Key

internal interface Codec {
    fun encrypt(message: String, key: Key): String
    fun decrypt(cipherText: String, key: Key): String
}