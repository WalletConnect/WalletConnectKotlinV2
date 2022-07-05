package com.walletconnect.sign.crypto.managers

import com.walletconnect.sign.core.model.vo.Key
import com.walletconnect.sign.crypto.KeyStore
import com.walletconnect.sign.util.bytesToHex
import com.walletconnect.sign.util.hexToBytes

internal class KeyChainMock : KeyStore {
    private val mapOfKeys = mutableMapOf<String, String>()

    override fun setSymmetricKey(tag: String, key: Key) {
        mapOfKeys[tag] = key.keyAsHex
    }

    override fun getSymmetricKey(tag: String): String {
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