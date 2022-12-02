@file:JvmSynthetic

package com.walletconnect.android.internal.common

import android.util.Base64
import com.walletconnect.android.internal.common.exception.CannotFindKeyPairException
import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.crypto.data.repository.BaseJwtRepository

internal class JwtRepositoryAndroid(private val keyChain: KeyStore) : BaseJwtRepository() {

    override fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey) {
        keyChain.setKeys(KEY_DID_KEYPAIR, privateKey, publicKey)
    }

    override fun encodeByteArray(signature: ByteArray): String {
        return Base64.encodeToString(signature, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    override fun getKeyPair(): Pair<String, String> {
        return if (doesKeyPairExist()) {
            val (privateKey, publicKey) = keyChain.getKeys(KEY_DID_KEYPAIR)
                ?: throw CannotFindKeyPairException("No key pair for given tag: $KEY_DID_KEYPAIR")
            publicKey to privateKey
        } else {
            getDIDFromNewKeyPair()
        }
    }

    private fun doesKeyPairExist(): Boolean {
        return keyChain.checkKeys(KEY_DID_KEYPAIR)
    }
}