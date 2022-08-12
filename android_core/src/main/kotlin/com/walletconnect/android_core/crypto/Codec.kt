@file:JvmSynthetic

package com.walletconnect.android_core.crypto

import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.android_core.common.model.vo.sync.ParticipantsVO
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: ParticipantsVO? = null): String
    fun decrypt(topic: Topic, cipherText: String, receiverPublicKey: PublicKey? = null): String
}