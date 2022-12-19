package com.walletconnect.android.impl.crypto

import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Topic

interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: Participants? = null): String
    fun decrypt(topic: Topic, cipherText: String): String
    fun decryptPayload(key: SymmetricKey, nonce: ByteArray, input: ByteArray): ByteArray

    companion object {
        const val NONCE_SIZE = 12
        const val KEY_SIZE = 32
        const val ENVELOPE_TYPE_SIZE = 1
    }
}