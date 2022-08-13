@file:JvmSynthetic

package com.walletconnect.sign.crypto

import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.sync.ParticipantsVO

internal interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: ParticipantsVO? = null): String
    fun decrypt(topic: Topic, cipherText: String, receiverPublicKey: PublicKey? = null): String
}