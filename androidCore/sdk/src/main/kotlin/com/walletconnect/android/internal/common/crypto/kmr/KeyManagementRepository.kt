package com.walletconnect.android.internal.common.crypto.kmr

import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

interface KeyManagementRepository {
    fun setKey(key: Key, tag: String)
    fun removeKeys(tag: String)

    fun getPublicKey(tag: String): PublicKey
    fun getSymmetricKey(tag: String): SymmetricKey
    fun getKeyPair(key: PublicKey): Pair<PublicKey, PrivateKey>

    fun generateAndStoreEd25519KeyPair(): PublicKey
    fun generateAndStoreX25519KeyPair(): PublicKey
    fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey)
    fun getSelfPublicFromKeyAgreement(topic: Topic): PublicKey

    fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey
    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey

    fun getTopicFromKey(key: Key): Topic
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
}