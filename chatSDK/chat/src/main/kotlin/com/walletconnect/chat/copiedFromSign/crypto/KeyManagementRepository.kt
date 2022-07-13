@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.crypto

import com.walletconnect.chat.copiedFromSign.core.model.vo.PrivateKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.SymmetricKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO

internal interface KeyManagementRepository {
    fun generateSymmetricKey(topic: TopicVO): SymmetricKey
    fun setSymmetricKey(topic: TopicVO, symmetricKey: SymmetricKey)
    fun getSymmetricKey(topic: TopicVO): SymmetricKey

    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): TopicVO
    fun getKeyAgreement(topic: TopicVO): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey

    fun removeKeys(tag: String)

    //Added with Chat SDK
    fun generateInviteSelfKeyPair(): Pair<PublicKey, PrivateKey>
    fun getInviteSelfPublicKey(): PublicKey
    fun setInviteSelfPublicKey(topic: TopicVO, publicKey: PublicKey)
    fun getHash(string: String): String
    fun getInvitePublicKey(topic: TopicVO): PublicKey
    fun setKeyAgreement(topic: TopicVO, self: PublicKey, peer: PublicKey)
}