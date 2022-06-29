@file:JvmSynthetic

package com.walletconnect.sign.crypto

import com.walletconnect.sign.core.exceptions.client.WalletConnectException
import com.walletconnect.sign.core.model.vo.Key

internal interface KeyStore {
    fun getSymmetricKey(tag: String): String
    fun setSymmetricKey(tag: String, key: Key)

    @Throws(WalletConnectException.InternalError::class)
    fun getKeys(tag: String): Pair<String, String>
    fun setKeys(tag: String, key1: Key, key2: Key)

    fun deleteKeys(tag: String)
}