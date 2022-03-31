package com.walletconnect.walletconnectv2.crypto.codec

import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.relay.data.codec.AuthenticatedEncryptionCodec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthenticatedEncryptionCodecTest {

    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()

    @Test
    fun `ChaCha20-Poly1305 encryption test`() {
        val sharedKey = SecretKey("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c")
        val message = "WalletConnect"

        val encryptedMessage = codec.encrypt(message, sharedKey)
        val text = codec.decrypt(encryptedMessage, sharedKey)
        assertEquals(text, message)
    }

    @Test
    fun `encrypt test`() {
        val sharedKey = SecretKey("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c")
        val message = "WalletConnect"
        val encryptedMessage = codec.encrypt(message, sharedKey)
        assertEquals("5JJK04yH8m/DmHWxvEQ7u09nkK1IxOJqlTJ1CqQ=", encryptedMessage)
    }

    @Test
    fun `decrypt test`() {
        val sharedKey = SecretKey("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c")
        val cipherText = "5JJK04yH8m/DmHWxvEQ7u09nkK1IxOJqlTJ1CqQ="
        val encryptedMessage = codec.decrypt(cipherText, sharedKey)
        assertEquals("WalletConnect", encryptedMessage)
    }
}