package com.walletconnect.android_core.crypto

import com.walletconnect.android_core.common.model.Participants
import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: Participants? = null): String
    fun decrypt(topic: Topic, cipherText: String, receiverPublicKey: PublicKey? = null): String
}