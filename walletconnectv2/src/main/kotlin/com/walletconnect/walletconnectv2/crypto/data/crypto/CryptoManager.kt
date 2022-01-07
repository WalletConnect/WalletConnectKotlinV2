package com.walletconnect.walletconnectv2.crypto.data.crypto

import com.walletconnect.walletconnectv2.common.model.Topic
import com.walletconnect.walletconnectv2.crypto.model.Key
import com.walletconnect.walletconnectv2.crypto.model.PublicKey
import com.walletconnect.walletconnectv2.crypto.model.SharedKey

interface CryptoManager {
    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, Topic>
    fun getKeyAgreement(topic: Topic): Pair<Key, Key>
    fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: Topic)
    fun removeKeys(tag: String)
}