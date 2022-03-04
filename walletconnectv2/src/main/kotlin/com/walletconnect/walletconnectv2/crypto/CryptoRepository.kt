package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO

internal interface CryptoRepository {
    fun generateSymmetricKey(topic: TopicVO): SecretKey
    fun setSymmetricKey(topic: TopicVO, symmetricKey: SecretKey)
//    fun getSymmetricKeys(topic: TopicVO): Pair<SymmetricKey, PublicKey>

    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SecretKey, TopicVO>

    fun getKeyAgreement(topic: TopicVO): Pair<SecretKey, PublicKey>
    fun setEncryptionKeys(sharedKey: SecretKey, publicKey: PublicKey, topic: TopicVO)
    fun removeKeys(tag: String)
}