package com.walletconnect.push.wallet.engine.domain

import com.walletconnect.android.impl.crypto.Codec

class DecryptMessageUseCase(private val codec: Codec) {

    operator fun invoke(key: String, message: String): String {
        return codec.decryptMessage(key, message)
    }
}