package com.walletconnect.android.impl.crypto

import com.walletconnect.android.impl.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

interface KeyManagementRepository {
    fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey
    fun generateSymmetricKey(): SymmetricKey
    fun setSymmetricKey(topic: Topic, symmetricKey: SymmetricKey)
    fun getSymmetricKey(topic: Topic): SymmetricKey

    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey
    fun generateTopicFromKeyAgreementAndSafeSymKey(self: PublicKey, peer: PublicKey): Topic
    fun getTopicFromKey(key: Key): Topic
    fun getKeyAgreement(topic: Topic): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey

    fun removeKeys(tag: String)
    fun setSelfParticipant(key: PublicKey, topic: Topic)
    fun getSelfParticipant(topic: Topic): PublicKey?

    //Added with Chat SDK
    fun generateInviteSelfKeyPair(): Pair<PublicKey, PrivateKey>
    fun getInviteSelfPublicKey(): PublicKey
    fun setInviteSelfPublicKey(topic: Topic, publicKey: PublicKey)
    fun getHash(string: String): String
    fun getInvitePublicKey(topic: Topic): PublicKey
    fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey)
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
}