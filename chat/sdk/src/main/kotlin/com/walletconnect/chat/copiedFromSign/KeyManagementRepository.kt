@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign

import com.walletconnect.android.impl.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal interface KeyManagementRepository {
    fun generateSymmetricKey(topic: Topic): SymmetricKey
    fun setSymmetricKey(topic: Topic, symmetricKey: SymmetricKey)
    fun getSymmetricKey(topic: Topic): SymmetricKey

    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
    fun getKeyAgreement(topic: Topic): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey

    fun removeKeys(tag: String)

    //Added with Chat SDK
    fun generateInviteSelfKeyPair(): Pair<PublicKey, PrivateKey>
    fun getInviteSelfPublicKey(): PublicKey
    fun setInviteSelfPublicKey(topic: Topic, publicKey: PublicKey)
    fun getHash(string: String): String
    fun getInvitePublicKey(topic: Topic): PublicKey
    fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey)
}