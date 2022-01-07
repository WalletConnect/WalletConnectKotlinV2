package com.walletconnect.walletconnectv2.crypto.data.crypto

import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.crypto.model.Key
import com.walletconnect.walletconnectv2.crypto.model.PublicKey
import com.walletconnect.walletconnectv2.crypto.model.SharedKey

interface CryptoManager {
    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, TopicVO>
    fun getKeyAgreement(topic: TopicVO): Pair<Key, Key>
    fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: TopicVO)
    fun removeKeys(tag: String)
}