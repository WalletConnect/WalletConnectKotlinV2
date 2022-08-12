package com.walletconnect.android_core.crypto.codec

import com.walletconnect.android_core.common.MissingParticipantsException
import com.walletconnect.android_core.common.MissingReceiverPublicKeyException
import com.walletconnect.android_core.common.model.SymmetricKey
import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.android_core.common.model.vo.sync.ParticipantsVO
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.android_core.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.android_core.utils.Empty
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.client.WalletConnectException
import com.walletconnect.sign.common.model.vo.sync.ParticipantsVO
import com.walletconnect.sign.crypto.KeyManagementRepository
import com.walletconnect.sign.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.utils.Empty
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ChaChaPolyCodecTest {

    private val keyManagementRepository: KeyManagementRepository = mockk()
    private val codec: ChaChaPolyCodec = ChaChaPolyCodec(keyManagementRepository)
    private val self = PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a")
    private val peer = PublicKey("1fb63fca5c6ac731246f2f069d3bc2454345d5208254aa8ea7bffc6d110c8862")
    private val participants = ParticipantsVO(senderPublicKey = self, receiverPublicKey = peer)
    private val symmetricKey = SymmetricKey(KEY_64_BYTES_1)
    private val invalidKey = SymmetricKey(KEY_64_BYTES_2)
    private val topic = Topic("topic")

    companion object {
        private const val MESSAGE = "mockMessage"
        private const val KEY_32_BYTES = "60b5a61613dd75aff6f2fd98585bd353"
        private const val KEY_64_BYTES_1 = "e002642656e99d802437eb2c7fcd151dc292cc25e54447bf2cc18153b71233de"
        private const val KEY_64_BYTES_2 = "386214f6925c951d10b2d590c7065ac5b8f29d94ab1dc08ed4d62d9c5c0841eb"
        private const val KEY_128_BYTES =
            "0621cfea34719bc40f6df59d1a33b67df7e27015778ad1d1c551f15b67456b70fa2720239e49d87fdf319ad2120d859bf1f77e659ec678bb5eaefc095e0860a1"
    }

    @Test
    fun `ChaCha20-Poly1305 encryption and decryption envelope type 0 with the same key`() {
        every { keyManagementRepository.getSymmetricKey(topic) } returns symmetricKey

        listOf("secretMessage", "", "👍🏻").forEach { message ->
            val encryptedMessage = codec.encrypt(topic, message, EnvelopeType.ZERO)
            assertEquals(message, codec.decrypt(topic, encryptedMessage))
        }
    }

    @Test
    fun `ChaCha20-Poly1305 encryption and decryption envelope type 1 with the same key`() {
        every { keyManagementRepository.getSymmetricKey(topic) } returns symmetricKey
        every { keyManagementRepository.generateSymmetricKeyFromKeyAgreement(self, peer) } returns symmetricKey
        every { keyManagementRepository.generateSymmetricKeyFromKeyAgreement(peer, self) } returns symmetricKey

        listOf("secretMessage", "", "👍🏻").forEach { message ->
            val encryptedMessage = codec.encrypt(topic, message, EnvelopeType.ONE, participants)
            assertEquals(message, codec.decrypt(topic, encryptedMessage, peer))
        }
    }

    @Test
    fun `ChaCha20-Poly1305 encryption envelope type 1 throws missing participants`() {
        assertThrows(MissingParticipantsException::class.java) {
            codec.encrypt(topic, MESSAGE, EnvelopeType.ONE)
        }
    }

    @Test
    fun `ChaCha20-Poly1305 decryption envelope type 1 throws missing receiver key`() {
        assertThrows(MissingReceiverPublicKeyException::class.java) {
            every { keyManagementRepository.getSymmetricKey(topic) } returns symmetricKey
            every { keyManagementRepository.generateSymmetricKeyFromKeyAgreement(self, peer) } returns symmetricKey

            val encryptedMessage = codec.encrypt(topic, MESSAGE, EnvelopeType.ONE, participants)
            codec.decrypt(topic, encryptedMessage)
        }
    }

    @Test
    fun `ChaCha20-Poly1305 encryption envelope type 0 with key that is not 64 Byte long throws`() {
        listOf(String.Empty, KEY_32_BYTES, KEY_128_BYTES).forEach { encryptionKeyAsHex ->
            every { keyManagementRepository.getSymmetricKey(topic) } returns SymmetricKey(encryptionKeyAsHex)
            assertThrows(Exception::class.java) {
                codec.encrypt(topic, MESSAGE, EnvelopeType.ZERO)
            }
        }
    }

    @Test
    fun `ChaCha20-Poly1305 encryption and decryption envelope type 0 with the different key throws`() {
        assertThrows(Exception::class.java) {
            every { keyManagementRepository.getSymmetricKey(topic) } returns symmetricKey
            val encryptedMessage = codec.encrypt(topic, MESSAGE, EnvelopeType.ZERO)

            every { keyManagementRepository.getSymmetricKey(topic) } returns invalidKey
            codec.decrypt(topic, encryptedMessage)
        }
    }

    @Test
    fun `ChaCha20-Poly1305 decryption envelope type 0 of non base64 encoded message throws `() {
        assertThrows(Exception::class.java) {
            codec.decrypt(topic, MESSAGE)
        }
    }
}