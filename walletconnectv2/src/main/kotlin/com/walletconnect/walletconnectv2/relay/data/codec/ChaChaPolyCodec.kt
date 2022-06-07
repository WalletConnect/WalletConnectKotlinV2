package com.walletconnect.walletconnectv2.relay.data.codec

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.util.hexToBytes
import com.walletconnect.walletconnectv2.util.randomBytes
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer

internal class ChaChaPolyCodec : Codec {

    override fun encrypt(message: String, key: Key): String {
        val input = message.toByteArray(Charsets.UTF_8)
        val nonceBytes = randomBytes(NONCE_SIZE)
        val cha20Poly1305 = ChaCha20Poly1305()
        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonceBytes)
        cha20Poly1305.init(true, params)
        val cipherText = ByteArray(cha20Poly1305.getOutputSize(input.size))
        val outputSize = cha20Poly1305.processBytes(input, 0, input.size, cipherText, 0)
        cha20Poly1305.doFinal(cipherText, outputSize)

        val output: ByteArray = ByteBuffer.allocate(cipherText.size + NONCE_SIZE)
            .put(nonceBytes)
            .put(cipherText)
            .array()

        return Base64.toBase64String(output)
    }

    override fun decrypt(cipherText: String, key: Key): String {
        val cipherTextBytes = Base64.decode(cipherText)
        val encryptedText = ByteArray(cipherTextBytes.size - NONCE_SIZE)
        val nonce = ByteArray(NONCE_SIZE)
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(cipherTextBytes)
        byteBuffer.get(nonce)
        byteBuffer.get(encryptedText)

        val cha20Poly1305 = ChaCha20Poly1305()
        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonce)
        cha20Poly1305.init(false, params)
        val cipherText = ByteArray(cha20Poly1305.getOutputSize(encryptedText.size))
        val outputSize = cha20Poly1305.processBytes(encryptedText, 0, encryptedText.size, cipherText, 0)
        cha20Poly1305.doFinal(cipherText, outputSize)

        return String(cipherText, Charsets.UTF_8)
    }

    companion object {
        private const val NONCE_SIZE = 12
    }
}