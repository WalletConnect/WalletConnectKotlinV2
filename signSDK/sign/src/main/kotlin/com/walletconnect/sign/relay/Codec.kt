package com.walletconnect.sign.relay

import com.walletconnect.sign.core.model.vo.Key

internal interface Codec {
    fun encrypt(message: String, key: Key): String
    fun decrypt(cipherText: String, key: Key): String
}