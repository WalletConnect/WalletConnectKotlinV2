package com.walletconnect.sign.crypto

import com.walletconnect.sign.core.model.vo.Key

internal interface KeyStore {
    fun setSecretKey(tag: String, key: Key)
    fun getSecretKey(tag: String): String

    fun setKeys(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>

    fun deleteKeys(tag: String)
}