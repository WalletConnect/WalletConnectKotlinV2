@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.crypto

import com.walletconnect.chat.copiedFromSign.core.model.vo.Key

internal interface KeyStore {
    fun setSymmetricKey(tag: String, key: Key)
    fun getSymmetricKey(tag: String): String

    fun setKeys(tag: String, key1: Key, key2: Key)
    fun getKeys(tag: String): Pair<String, String>

    fun deleteKeys(tag: String)

    // Added With Chat SDK
    fun getInviteSelfPublicKey(tag: String): String?
}