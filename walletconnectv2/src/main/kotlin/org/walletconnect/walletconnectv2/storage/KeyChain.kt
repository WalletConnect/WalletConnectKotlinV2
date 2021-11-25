package org.walletconnect.walletconnectv2.storage

import com.goterl.lazysodium.utils.HexMessageEncoder
import org.walletconnect.walletconnectv2.crypto.data.Key
import org.walletconnect.walletconnectv2.sharedPreferences
import org.walletconnect.walletconnectv2.util.empty

class KeyChain : KeyStore {

    override fun setKey(tag: String, key1: Key, key2: Key) {
        val keys = concatKeys(key1, key2)
        sharedPreferences.edit().putString(tag, keys).apply()
    }

    override fun getKeys(tag: String): Pair<String, String> {
        val concatKeys = sharedPreferences.getString(tag, String.empty) ?: String.empty
        return splitKeys(concatKeys)
    }

    override fun deleteKeys(tag: String) {
        sharedPreferences.edit().remove(tag).apply()
    }

    private fun concatKeys(keyA: Key, keyB: Key): String = with(HexMessageEncoder()) {
        encode(decode(keyA.keyAsHex) + decode(keyB.keyAsHex))
    }

    private fun splitKeys(concatKeys: String): Pair<String, String> = with(HexMessageEncoder()) {
        val concatKeysByteArray = decode(concatKeys)
        val privateKeyByteArray = concatKeysByteArray.sliceArray(0 until (concatKeysByteArray.size / 2))
        val publicKeyByteArray = concatKeysByteArray.sliceArray((concatKeysByteArray.size / 2) until concatKeysByteArray.size)
        return encode(privateKeyByteArray) to encode(publicKeyByteArray)
    }
}