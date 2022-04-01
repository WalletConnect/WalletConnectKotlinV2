package com.walletconnect.walletconnectv2.crypto.codec

import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.relay.data.codec.ChaChaPolyCodec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChaChaPolyCodecTest {

    private val codec: ChaChaPolyCodec = ChaChaPolyCodec()

    @Test
    fun `ChaCha20-Poly1305 encryption test`() {
        val sharedKey = SecretKey("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c")
        val message = "WalletConnect"

        val encryptedMessage = codec.encrypt(message, sharedKey)
        val text = codec.decrypt(encryptedMessage, sharedKey)

        assertEquals(text, message)
    }

    @Test
    fun `decrypt test`() {
        val sharedKey = SecretKey("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c")
        val cipherText = "cXdlY2ZhYXNkYWRzVhkbjHqli8hN0rFbAtMPIsJho4zLvWskMTQKSGw="
        val encryptedMessage = codec.decrypt(cipherText, sharedKey)

        assertEquals("WalletConnect", encryptedMessage)
    }
}