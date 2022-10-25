package com.walletconnect.android.internal.common.crypto

import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import kotlin.reflect.KClass

interface KeyManagementRepository {
    fun setKey(key: Key, tag: String)
    fun <T : Key> getKey(tag: String, clazz: KClass<T>): Key
    fun removeKeys(tag: String)

    fun generateKeyPair(): PublicKey
    fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey)
    fun getKeyAgreement(topic: Topic): Pair<PublicKey, PublicKey>

    fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey
    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey

    fun getTopicFromKey(key: Key): Topic
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
}