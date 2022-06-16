@file:JvmSynthetic

package com.walletconnect.sign.relay.data.codec

import com.walletconnect.sign.core.model.vo.Key
import com.walletconnect.sign.relay.Codec
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.sign.util.hexToBytes
import com.walletconnect.sign.util.randomBytes
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer

internal class ChaChaPolyCodec : Codec {

    private val cha20Poly1305 = ChaCha20Poly1305()

    override fun encrypt(message: String, key: Key): String {
        val input = message.toByteArray(Charsets.UTF_8)
        val nonceBytes = randomBytes(NONCE_SIZE)

        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonceBytes)
        cha20Poly1305.init(true, params) //note: in the debugging mode code throws InvalidArgumentException but it doesn't affects the final method execution
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
        val envelopeType = cipherTextBytes[0]

        return when (envelopeType){
            EnvelopeTypes.TYPE_0 -> decryptType0(cipherTextBytes, key)
            EnvelopeTypes.TYPE_1 -> decryptType1(cipherTextBytes)
            else -> String.Empty
        }
    }

    private fun decryptType0(cipherTextBytes: ByteArray, key: Key): String {
        val encryptedText = ByteArray(cipherTextBytes.size - NONCE_SIZE - EnvelopeTypes.SIZE)
        val nonce = ByteArray(NONCE_SIZE)
        val envelopeType = ByteArray(EnvelopeTypes.SIZE)
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(cipherTextBytes)
        byteBuffer.get(envelopeType)
        byteBuffer.get(nonce)
        byteBuffer.get(encryptedText)

        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonce)
        cha20Poly1305.init(false, params) //note: in the debugging mode code throws InvalidArgumentException but it doesn't affects the final method execution
        val cipherTextByteArray = ByteArray(cha20Poly1305.getOutputSize(encryptedText.size))
        val outputSize = cha20Poly1305.processBytes(encryptedText, 0, encryptedText.size, cipherTextByteArray, 0)
        cha20Poly1305.doFinal(cipherTextByteArray, outputSize)

        return String(cipherTextByteArray, Charsets.UTF_8)
    }


    private fun decryptType1(cipherTextBytes: ByteArray): String {
        val encryptedText = ByteArray(cipherTextBytes.size - NONCE_SIZE - KEY_SIZE - EnvelopeTypes.SIZE)
        val nonce = ByteArray(NONCE_SIZE)
        val envelopeType = ByteArray(EnvelopeTypes.SIZE)
        val publicKey = ByteArray(KEY_SIZE)
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(cipherTextBytes)
        byteBuffer.get(envelopeType)
        byteBuffer.get(publicKey)
        byteBuffer.get(nonce)
        byteBuffer.get(encryptedText)

        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonce)
        cha20Poly1305.init(false, params) //note: in the debugging mode code throws InvalidArgumentException but it doesn't affects the final method execution
        val cipherTextByteArray = ByteArray(cha20Poly1305.getOutputSize(encryptedText.size))
        val outputSize = cha20Poly1305.processBytes(encryptedText, 0, encryptedText.size, cipherTextByteArray, 0)
        cha20Poly1305.doFinal(cipherTextByteArray, outputSize)

        return String(cipherTextByteArray, Charsets.UTF_8)
    }

    private object EnvelopeTypes {
        const val TYPE_0: Byte = 0
        const val TYPE_1: Byte = 1

        const val SIZE = 1
    }

    companion object {
        private const val NONCE_SIZE = 12
        private const val KEY_SIZE = 64
    }
}