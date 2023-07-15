package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes

internal class KeyChainMock : KeyStore {
    private val mapOfKeys = mutableMapOf<String, String>()

    override fun setKey(tag: String, key: Key) {
        mapOfKeys[tag] = key.keyAsHex
    }

    override fun getKey(tag: String): String {
        return mapOfKeys[tag] ?: ""
    }


    override fun setKeys(tag: String, key1: Key, key2: Key) {
        val keys = concatKeys(key1, key2)
        mapOfKeys[tag] = keys
    }

    override fun getKeys(tag: String): Pair<String, String> {
        val keys = mapOfKeys[tag] ?: ""
        return splitKeys(keys)
    }

    override fun deleteKeys(tag: String) {
        mapOfKeys.remove(tag)
    }

    override fun checkKeys(tag: String): Boolean {
        return mapOfKeys.containsKey(tag)
    }

    private fun concatKeys(keyA: Key, keyB: Key): String = (keyA.keyAsHex.hexToBytes() + keyB.keyAsHex.hexToBytes()).bytesToHex()

    private fun splitKeys(concatKeys: String): Pair<String, String> {
        val concatKeysByteArray = concatKeys.hexToBytes()
        val privateKeyByteArray = concatKeysByteArray.sliceArray(0 until (concatKeysByteArray.size / 2))
        val publicKeyByteArray = concatKeysByteArray.sliceArray((concatKeysByteArray.size / 2) until concatKeysByteArray.size)
        return privateKeyByteArray.bytesToHex() to publicKeyByteArray.bytesToHex()
    }
}