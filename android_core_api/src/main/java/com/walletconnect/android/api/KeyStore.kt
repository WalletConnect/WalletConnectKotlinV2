@file:JvmSynthetic

package com.walletconnect.android.api

import com.walletconnect.foundation.common.model.Key

interface KeyStore {
    fun getKey(tag: String): String
    fun setKey(tag: String, key: Key)

    @Throws(InternalError::class)
    fun getKeys(tag: String): Pair<String, String>
    fun setKeys(tag: String, key1: Key, key2: Key)

    fun deleteKeys(tag: String)
    fun checkKeys(tag: String): Boolean
}