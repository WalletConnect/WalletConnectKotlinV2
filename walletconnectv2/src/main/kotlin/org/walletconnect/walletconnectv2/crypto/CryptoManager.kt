package org.walletconnect.walletconnectv2.crypto

import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.PublicKey

interface CryptoManager {

    fun hasKeys(tag: String): Boolean

    fun generateKeyPair(): PublicKey

    fun generateTopicAndSharedKey(
        self: PublicKey,
        peer: PublicKey,
        overrideTopic: String? = null
    ): Pair<String, Topic>

    fun getSharedKey(self: PublicKey, peer: PublicKey): String

    fun getKeyAgreement(topic: Topic): Pair<String, PublicKey>

    fun setEncryptionKeys(sharedKey: String, publicKey: PublicKey, topic: Topic)
}