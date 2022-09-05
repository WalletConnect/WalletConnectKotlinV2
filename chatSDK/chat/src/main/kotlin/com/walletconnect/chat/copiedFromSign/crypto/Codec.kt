@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.crypto

import com.walletconnect.chat.copiedFromSign.core.model.type.enums.EnvelopeType
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.ParticipantsVO

internal interface Codec {
    fun encrypt(topic: TopicVO, payload: String, envelopeType: EnvelopeType, participants: ParticipantsVO? = null): String
    fun decrypt(topic: TopicVO, cipherText: String): String
}