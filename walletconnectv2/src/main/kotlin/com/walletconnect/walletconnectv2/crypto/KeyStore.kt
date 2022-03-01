package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.core.model.vo.SymmetricKey

internal interface KeyStore {
    fun setSymmetricKey(topic: String, key: SymmetricKey)
    fun getSymmetricKey(topic: String): SymmetricKey

    fun setKeys(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>

    fun deleteKeys(tag: String)
}