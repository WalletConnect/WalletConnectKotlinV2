package com.walletconnect.android.impl.crypto

import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.foundation.common.model.Topic

interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: Participants? = null): String
    fun decrypt(topic: Topic, cipherText: String): String
    fun decryptMessage(key: String, cipherText: String): String //TODO: Maybe this needs to be an extension function instead
}