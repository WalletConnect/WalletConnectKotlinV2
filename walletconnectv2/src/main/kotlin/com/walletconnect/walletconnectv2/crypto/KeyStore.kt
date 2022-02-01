package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.core.model.vo.Key

internal interface KeyStore {
    fun setKey(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>
    fun deleteKeys(tag: String)
}