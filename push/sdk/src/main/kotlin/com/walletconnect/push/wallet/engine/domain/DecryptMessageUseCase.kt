package com.walletconnect.push.wallet.engine.domain

import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer

class DecryptMessageUseCase(private val codec: Codec, private val keyManagementRepository: KeyManagementRepository) {

    // TODO: Can we just use Codec.decrypt()?
    operator fun invoke(topic: String, message: String): String {
        val encryptedPayloadBytes = Base64.decode(message)
        val envelopeType = ByteArray(Codec.ENVELOPE_TYPE_SIZE)
        val nonce = ByteArray(Codec.NONCE_SIZE)
        val encryptedMessageBytes = ByteArray(encryptedPayloadBytes.size - Codec.NONCE_SIZE - Codec.ENVELOPE_TYPE_SIZE)

        //tp + iv + sb
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(encryptedPayloadBytes)
        byteBuffer.get(envelopeType)
        byteBuffer.get(nonce)
        byteBuffer.get(encryptedMessageBytes)

        val symmetricKey = keyManagementRepository.getSymmetricKey(topic)
        val decryptedTextBytes = codec.decryptPayload(symmetricKey, nonce, encryptedMessageBytes)

        return String(decryptedTextBytes, Charsets.UTF_8)
    }
}