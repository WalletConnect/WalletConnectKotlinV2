package com.walletconnect.walletconnectv2.relay.data.codec

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.util.hexToBytes
import com.walletconnect.walletconnectv2.util.randomBytes
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class ChaChaPolyCodec : Codec {

    override fun encrypt(message: String, key: Key): String {
        val data = message.toByteArray(Charsets.UTF_8)
        val nonceBytes = randomBytes(NONCE_SIZE)
        val cipher: Cipher = Cipher.getInstance(CHA_CHA_POLY)
        val ivParameterSpec: AlgorithmParameterSpec = IvParameterSpec(nonceBytes)
        val keySpec = SecretKeySpec(key.keyAsHex.hexToBytes(), CHA_CHA_20)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
        val cipherTextInBytes: ByteArray = cipher.doFinal(data)

        val output: ByteArray = ByteBuffer.allocate(cipherTextInBytes.size + NONCE_SIZE)
            .put(nonceBytes)
            .put(cipherTextInBytes)
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

        val ivParameterSpec: AlgorithmParameterSpec = IvParameterSpec(nonce)
        val keySpec = SecretKeySpec(key.keyAsHex.hexToBytes(), CHA_CHA_20)
        val cipher = Cipher.getInstance(CHA_CHA_POLY)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
        val message = cipher.doFinal(encryptedText)

        return String(message, Charsets.UTF_8)
    }

    companion object {
        private const val CHA_CHA_POLY = "ChaCha20-Poly1305"
        private const val CHA_CHA_20 = "ChaCha20"
        private const val NONCE_SIZE = 12
    }
}