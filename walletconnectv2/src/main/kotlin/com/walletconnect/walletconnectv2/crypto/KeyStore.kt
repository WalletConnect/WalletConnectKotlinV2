package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.common.model.vo.Key

interface KeyStore {
    fun setKey(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>
    fun deleteKeys(tag: String)
}