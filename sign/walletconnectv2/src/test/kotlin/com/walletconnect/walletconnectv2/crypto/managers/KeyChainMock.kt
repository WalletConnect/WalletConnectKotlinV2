package com.walletconnect.walletconnectv2.crypto.managers

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.util.bytesToHex
import com.walletconnect.walletconnectv2.util.hexToBytes

internal class KeyChainMock : KeyStore {

    private val mapOfKeys = mutableMapOf<String, String>()

    override fun setSecretKey(tag: String, key: Key) {
        mapOfKeys[tag] = key.keyAsHex
    }

    override fun getSecretKey(tag: String): String {
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

    private fun concatKeys(keyA: Key, keyB: Key): String = (keyA.keyAsHex.hexToBytes() + keyB.keyAsHex.hexToBytes()).bytesToHex()

    private fun splitKeys(concatKeys: String): Pair<String, String> {
        val concatKeysByteArray = concatKeys.hexToBytes()
        val privateKeyByteArray = concatKeysByteArray.sliceArray(0 until (concatKeysByteArray.size / 2))
        val publicKeyByteArray = concatKeysByteArray.sliceArray((concatKeysByteArray.size / 2) until concatKeysByteArray.size)
        return privateKeyByteArray.bytesToHex() to publicKeyByteArray.bytesToHex()
    }
}