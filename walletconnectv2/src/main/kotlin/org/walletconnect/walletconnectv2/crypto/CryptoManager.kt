package org.walletconnect.walletconnectv2.crypto

import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.Key
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey

interface CryptoManager {
    fun generateKeyPair(): PublicKey
    fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, Topic>
    fun getKeyAgreement(topic: Topic): Pair<Key, Key>
    fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: Topic)
    fun removeKeys(tag: String)
}