package com.walletconnect.push.wallet.engine.domain

import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.foundation.common.model.Topic

class DecryptMessageUseCase(private val codec: Codec) {

    operator fun invoke(topic: String, message: String): String {
        return codec.decrypt(Topic(topic), message)
    }
}