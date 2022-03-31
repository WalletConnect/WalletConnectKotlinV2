package com.walletconnect.walletconnectv2.relay.data.codec

import com.walletconnect.walletconnectv2.core.model.vo.Key
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.util.hexToBytes
import org.bouncycastle.util.encoders.Base64
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class AuthenticatedEncryptionCodec : Codec {

    override fun encrypt(message: String, key: Key): String {
        val data = message.toByteArray(Charsets.UTF_8)
        val nonceBytes = ByteArray(NONCE_SIZE)
        val cipher: Cipher = Cipher.getInstance(CHA_CHA_POLY)
        val ivParameterSpec: AlgorithmParameterSpec = IvParameterSpec(nonceBytes)
        val keySpec = SecretKeySpec(key.keyAsHex.hexToBytes(), CHA_CHA_20)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
        val cipherTextInBytes: ByteArray = cipher.doFinal(data)

        return Base64.toBase64String(cipherTextInBytes)
    }

    override fun decrypt(cipherText: String, key: Key): String {
        val nonceBytes = ByteArray(NONCE_SIZE)
        val ivParameterSpec: AlgorithmParameterSpec = IvParameterSpec(nonceBytes)
        val keySpec = SecretKeySpec(key.keyAsHex.hexToBytes(), CHA_CHA_20)
        val cipher = Cipher.getInstance(CHA_CHA_POLY)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
        val cipherTextBytes = Base64.decode(cipherText)
        val message = cipher.doFinal(cipherTextBytes)

        return String(message, Charsets.UTF_8)
    }

    companion object {
        private const val CHA_CHA_POLY = "ChaCha20-Poly1305" ///None/NoPadding")
        private const val CHA_CHA_20 = "ChaCha20"
        private const val NONCE_SIZE = 12
    }
}