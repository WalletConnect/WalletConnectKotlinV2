package com.walletconnect.sign.crypto.codec

import com.walletconnect.sign.core.model.vo.SecretKey
import com.walletconnect.sign.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.sign.util.Empty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChaChaPolyCodecTest {

    private val codec: ChaChaPolyCodec = ChaChaPolyCodec()
    private val sharedKey = SecretKey(KEY_64_BYTES_1)
    private val invalidKey = SecretKey(KEY_64_BYTES_2)

    companion object {
        private const val MESSAGE = "mockMessage"
        private const val KEY_32_BYTES = "60b5a61613dd75aff6f2fd98585bd353"
        private const val KEY_64_BYTES_1 = "e002642656e99d802437eb2c7fcd151dc292cc25e54447bf2cc18153b71233de"
        private const val KEY_64_BYTES_2 = "386214f6925c951d10b2d590c7065ac5b8f29d94ab1dc08ed4d62d9c5c0841eb"
        private const val KEY_128_BYTES = "0621cfea34719bc40f6df59d1a33b67df7e27015778ad1d1c551f15b67456b70fa2720239e49d87fdf319ad2120d859bf1f77e659ec678bb5eaefc095e0860a1"
    }

    @Test
    fun `ChaCha20-Poly1305 encryption and decryption with the same key`() {
        listOf("secretMessage", "", "👍🏻").forEach { message ->
            val encryptedMessage = codec.encrypt(message, sharedKey)
            assertEquals(message, codec.decrypt(encryptedMessage, sharedKey))
        }
    }

    @Test
    fun `ChaCha20-Poly1305 encryption with key that is not 64 Byte long throws`() {
        listOf(String.Empty, KEY_32_BYTES, KEY_128_BYTES).forEach { encryptionKeyAsHex ->
            Assertions.assertThrows(Exception::class.java) {
                codec.encrypt(MESSAGE, SecretKey(encryptionKeyAsHex))
            }
        }
    }

    @Test
    fun `ChaCha20-Poly1305 encryption and decryption with the different key throws`() {
        Assertions.assertThrows(Exception::class.java) {
            val encryptedMessage = codec.encrypt(MESSAGE, sharedKey)
            codec.decrypt(encryptedMessage, invalidKey)
        }
    }

    @Test
    fun `ChaCha20-Poly1305 decryption of non base64 encoded message throws `() {
        Assertions.assertThrows(Exception::class.java) {
            codec.decrypt(MESSAGE, sharedKey)
        }
    }
}