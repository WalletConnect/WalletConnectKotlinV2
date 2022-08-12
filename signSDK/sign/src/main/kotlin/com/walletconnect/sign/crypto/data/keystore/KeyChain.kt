@file:JvmSynthetic

package com.walletconnect.sign.crypto.data.keystore

import android.content.SharedPreferences
import com.walletconnect.android_core.common.exceptions.client.WalletConnectException
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.sign.crypto.KeyStore
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import com.walletconnect.utils.Empty

internal class KeyChain(private val sharedPreferences: SharedPreferences) : KeyStore {

    override fun setSymmetricKey(tag: String, key: Key) {
        sharedPreferences.edit().putString(tag, key.keyAsHex).apply()
    }

    override fun getSymmetricKey(tag: String): String {
        return sharedPreferences.getString(tag, String.Empty) ?: String.Empty
    }

    override fun setKeys(tag: String, keyA: Key, keyB: Key) {
        val keys = concatKeys(keyA, keyB)
        sharedPreferences.edit().putString(tag, keys).apply()
    }

    @Throws(WalletConnectException.InternalError::class)
    override fun getKeys(tag: String): Pair<String, String> {
        val concatKeys = sharedPreferences.getString(tag, null) ?: throw WalletConnectException.InternalError("unable to find keys")
        return splitKeys(concatKeys)
    }

    override fun deleteKeys(tag: String) {
        sharedPreferences.edit().remove(tag).apply()
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