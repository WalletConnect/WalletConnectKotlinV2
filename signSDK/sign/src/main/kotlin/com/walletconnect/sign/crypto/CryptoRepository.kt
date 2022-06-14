package com.walletconnect.sign.crypto

import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.core.model.vo.SecretKey
import com.walletconnect.sign.core.model.vo.TopicVO

internal interface CryptoRepository {
    fun setSymmetricKey(topic: TopicVO, symmetricKey: SecretKey)
    fun getSymmetricKey(topic: TopicVO): SecretKey

    fun getKeyAgreement(topic: TopicVO): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SecretKey, TopicVO>
    fun generateSymmetricKey(topic: TopicVO): SecretKey

    fun removeKeys(tag: String)
}