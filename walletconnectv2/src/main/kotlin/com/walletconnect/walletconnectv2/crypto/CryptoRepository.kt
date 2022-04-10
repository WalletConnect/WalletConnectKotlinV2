package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO

internal interface CryptoRepository {
    fun setSymmetricKey(topic: TopicVO, symmetricKey: SecretKey)
    fun getSecretKey(topic: TopicVO): SecretKey

    fun getKeyAgreement(topic: TopicVO): Pair<SecretKey, PublicKey>

    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SecretKey, TopicVO>
    fun generateSymmetricKey(topic: TopicVO): SecretKey

    fun removeKeys(tag: String)
}