package com.walletconnect.push.wallet.engine.domain

import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer

class DecryptMessageUseCase(private val codec: Codec) {

    operator fun invoke(topic: String, message: String): String {
        return codec.decrypt(Topic(topic), message)
    }
}