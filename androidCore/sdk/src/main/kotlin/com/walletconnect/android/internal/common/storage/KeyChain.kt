@file:JvmSynthetic

package com.walletconnect.android.internal.common.storage

import android.content.SharedPreferences
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.util.Empty
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes

internal class KeyChain(private val sharedPreferences: SharedPreferences) : KeyStore {

    override fun setKey(tag: String, key: Key) {
        sharedPreferences.edit().putString(tag, key.keyAsHex).apply()
    }

    override fun getKey(tag: String): String? {
        return sharedPreferences.getString(tag, null)
    }

    override fun setKeys(tag: String, key1: Key, key2: Key) {
        val keys = concatKeys(key1, key2)
        sharedPreferences.edit().putString(tag, keys).apply()
    }

    @Throws(InternalError::class)
    override fun getKeys(tag: String): Pair<String, String> {
        val concatKeys = sharedPreferences.getString(tag, null) ?: throw InternalError("Unable to find keys")
        return splitKeys(concatKeys)
    }

    override fun deleteKeys(tag: String) {
        if (checkKeys(tag)) {
            sharedPreferences.edit().remove(tag).apply()
        }
    }

    override fun checkKeys(tag: String): Boolean {
        return sharedPreferences.contains(tag)
    }

    private fun concatKeys(keyA: Key, keyB: Key): String = (keyA.keyAsHex.hexToBytes() + keyB.keyAsHex.hexToBytes()).bytesToHex()

    private fun splitKeys(concatKeys: String): Pair<String, String> {
        val concatKeysByteArray = concatKeys.hexToBytes()
        val keyA = concatKeysByteArray.sliceArray(0 until (concatKeysByteArray.size / 2))
        val keyB = concatKeysByteArray.sliceArray((concatKeysByteArray.size / 2) until concatKeysByteArray.size)

        return keyA.bytesToHex() to keyB.bytesToHex()
    }
}