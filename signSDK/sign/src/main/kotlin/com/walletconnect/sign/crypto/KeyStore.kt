@file:JvmSynthetic

package com.walletconnect.sign.crypto

import com.walletconnect.sign.core.model.vo.Key

internal interface KeyStore {
    fun setSymmetricKey(tag: String, key: Key)
    fun getSymmetricKey(tag: String): String

    fun setKeys(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>

    fun deleteKeys(tag: String)
}