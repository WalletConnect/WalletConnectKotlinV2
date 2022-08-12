@file:JvmSynthetic

package com.walletconnect.android_core.crypto

import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.core.model.vo.SymmetricKey

internal interface KeyManagementRepository {
    fun generateSymmetricKey(topic: Topic): SymmetricKey
    fun setSymmetricKey(topic: Topic, symmetricKey: SymmetricKey)
    fun getSymmetricKey(topic: Topic): SymmetricKey

    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
    fun getKeyAgreement(topic: Topic): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey

    fun removeKeys(tag: String)

    // Needs to be added with Chat SDK
}