package com.walletconnect.walletconnectv2.storage

import com.walletconnect.walletconnectv2.crypto.data.Key

interface KeyStore {
    fun setKey(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>
    fun deleteKeys(tag: String)
}