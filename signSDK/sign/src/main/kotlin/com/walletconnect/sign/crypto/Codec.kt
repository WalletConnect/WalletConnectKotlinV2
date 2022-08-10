@file:JvmSynthetic

package com.walletconnect.sign.crypto

import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.sync.ParticipantsVO

internal interface Codec {
    fun encrypt(topic: TopicVO, payload: String, envelopeType: EnvelopeType, participants: ParticipantsVO? = null): String
    fun decrypt(topic: TopicVO, cipherText: String, receiverPublicKey: PublicKey? = null): String
}