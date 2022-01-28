package com.walletconnect.walletconnectv2.crypto

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SharedKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO

internal interface CryptoRepository {
    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, TopicVO>
    fun getKeyAgreement(topic: TopicVO): Pair<Key, Key>
    fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: TopicVO)
    fun removeKeys(tag: String)
}