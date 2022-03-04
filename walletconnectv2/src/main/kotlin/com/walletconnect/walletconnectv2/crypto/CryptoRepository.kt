package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.core.model.vo.*

internal interface CryptoRepository {
    fun generateSymmetricKey(topic: TopicVO): SymmetricKey
    fun setSymmetricKey(topic: TopicVO, symmetricKey: SymmetricKey)
    fun getSymmetricKeys(topic: TopicVO): Pair<SymmetricKey, PublicKey>

    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, TopicVO>
    fun getKeyAgreement(topic: TopicVO): Pair<Key, Key>
    fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: TopicVO)

    fun removeKeys(tag: String)
}