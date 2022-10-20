@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign

import android.content.SharedPreferences
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.util.Empty
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes

//todo: use KeyChain from android_common
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


    override fun getKeys(tag: String): Pair<String, String> {
        val concatKeys = sharedPreferences.getString(tag, String.Empty) ?: String.Empty
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

    // Added With Chat SDK
    override fun getInviteSelfPublicKey(tag: String): String? {
        return sharedPreferences.getString(tag, null)
    }

    // Added With Chat SDK
    override fun setInviteSelfPublicKey(tag: String, key: Key) {
        sharedPreferences.edit().putString(tag, key.keyAsHex).apply()

    }

    // Added With Chat SDK
    override fun getPublicKey(tag: String): String {
        return sharedPreferences.getString(tag, String.Empty) ?: String.Empty
    }

    // Added With Chat SDK
    override fun setPublicKey(tag: String, publicKey: PublicKey) {
        sharedPreferences.edit().putString(tag, publicKey.keyAsHex).apply()
    }
}