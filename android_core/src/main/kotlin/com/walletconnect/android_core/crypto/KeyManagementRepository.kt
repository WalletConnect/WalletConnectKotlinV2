package com.walletconnect.android_core.crypto

import com.walletconnect.android_core.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

interface KeyManagementRepository {
    fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey
    fun generateSymmetricKey(): SymmetricKey
    fun setSymmetricKey(topic: Topic, symmetricKey: SymmetricKey)
    fun getSymmetricKey(topic: Topic): SymmetricKey

    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
    fun getTopicFromKey(key: Key): Topic
    fun getKeyAgreement(topic: Topic): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey

    fun removeKeys(tag: String)
    fun setSelfParticipant(key: PublicKey, topic: Topic)
    fun getSelfParticipant(topic: Topic): PublicKey?
}