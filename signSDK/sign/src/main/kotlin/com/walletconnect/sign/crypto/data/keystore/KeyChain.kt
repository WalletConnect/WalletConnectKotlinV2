@file:JvmSynthetic

package com.walletconnect.sign.crypto.data.keystore

import android.content.SharedPreferences
import com.walletconnect.sign.core.exceptions.client.WalletConnectException
import com.walletconnect.sign.core.model.vo.Key
import com.walletconnect.sign.crypto.KeyStore
import com.walletconnect.sign.util.Empty
import com.walletconnect.sign.util.bytesToHex
import com.walletconnect.sign.util.hexToBytes

internal class KeyChain(private val sharedPreferences: SharedPreferences) : KeyStore {

    override fun setSymmetricKey(tag: String, key: Key) {
        sharedPreferences.edit().putString(tag, key.keyAsHex).apply()
    }

    override fun getSymmetricKey(tag: String): String {
        return sharedPreferences.getString(tag, String.Empty) ?: String.Empty
    }

    override fun setKeys(tag: String, key1: Key, key2: Key) {
        val keys = concatKeys(key1, key2)
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

    private fun concatKeys(keyA: Key, keyB: Key): String = (keyA.keyAsHex.hexToBytes() + keyB.keyAsHex.hexToBytes()).bytesToHex()

    private fun splitKeys(concatKeys: String): Pair<String, String> {
        val concatKeysByteArray = concatKeys.hexToBytes()
        val privateKeyByteArray = concatKeysByteArray.sliceArray(0 until (concatKeysByteArray.size / 2))
        val publicKeyByteArray = concatKeysByteArray.sliceArray((concatKeysByteArray.size / 2) until concatKeysByteArray.size)
        return privateKeyByteArray.bytesToHex() to publicKeyByteArray.bytesToHex()
    }
}