@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign

import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PublicKey

internal interface KeyStore {
    fun setSymmetricKey(tag: String, key: Key)
    fun getSymmetricKey(tag: String): String

    fun setKeys(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>

    fun deleteKeys(tag: String)

    // Added With Chat SDK
    fun getInviteSelfPublicKey(tag: String): String?
    fun setInviteSelfPublicKey(tag: String, key: Key)
    fun getPublicKey(tag: String): String
    fun setPublicKey(tag: String, publicKey: PublicKey)
}