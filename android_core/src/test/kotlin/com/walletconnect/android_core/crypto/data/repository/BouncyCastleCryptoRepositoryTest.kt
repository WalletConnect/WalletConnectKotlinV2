package com.walletconnect.android_core.crypto.data.repository

import com.walletconnect.android_core.crypto.KeyStore
import com.walletconnect.android_core.crypto.managers.KeyChainMock
import com.walletconnect.android_core.utils.Empty
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.crypto.KeyStore
import com.walletconnect.sign.crypto.managers.KeyChainMock
import com.walletconnect.utils.Empty
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BouncyCastleCryptoRepositoryTest {
    private val publicKey = PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a")
    private val privateKey = PrivateKey("36bf507903537de91f5e573666eaa69b1fa313974f23b2b59645f20fea505854")
    private val keyChain: KeyStore = KeyChainMock()
    private val sut = spyk(BouncyCastleKeyManagementRepository(keyChain), recordPrivateCalls = true)
    private val topicVO = Topic("topic")

    @BeforeEach
    fun setUp() {
        sut.setKeyPair(publicKey, privateKey)
    }

    @Test
    fun `Verify that the generated public key has valid length`() {
        val publicKey = sut.generateKeyPair()
        assert(publicKey.keyAsHex.length == 64)
    }

    @Test
    fun `Generate shared key test`() {
        val peerKey = PublicKey("1fb63fca5c6ac731246f2f069d3bc2454345d5208254aa8ea7bffc6d110c8862")
        val result = sut.generateSymmetricKeyFromKeyAgreement(publicKey, peerKey)

        assert(result.keyAsHex.length == 64)
        assertEquals("b6382630866cb15f6ad2858d0ed8857029b0a7d0655bdf7a43b2bca86f6d966d", result.keyAsHex.lowercase())
    }

    @Test
    fun `Generate symmetric key test`() {
        val symKey = sut.generateSymmetricKey(topicVO)
        assert(symKey.keyAsHex.length == 64)

        val secretKey = sut.getSymmetricKey(topicVO)
        assertEquals(symKey.keyAsHex, secretKey.keyAsHex)
        assert(secretKey.keyAsHex.length == 64)
    }

    @Test
    fun `Generate a shared key and return a Topic object`() {
        val peerKey = PublicKey("ff7a7d5767c362b0a17ad92299ebdb7831dcbd9a56959c01368c7404543b3342")
        val topic = sut.generateTopicFromKeyAgreement(publicKey, peerKey)

        assert(topic.value.isNotBlank())
        assert(topic.value.length == 64)

        assertEquals("2c03712132ad2f85adc472a2242e608d67bfecd4362d05012d69a89143fecd16", topic.value)
    }

    @Test
    fun `SetKeyPair sets the concatenated keys to storage`() {
        assertNotNull(sut.getKeyPair(publicKey))
        assertEquals(publicKey.keyAsHex, keyChain.getKeys(publicKey.keyAsHex).first)
        assertEquals(privateKey.keyAsHex, keyChain.getKeys(publicKey.keyAsHex).second)
    }

    @Test
    fun `GetKeyPair gets a pair of PublicKey and PrivateKey when using a PublicKey as the key`() {
        val (testPublicKey, testPrivateKey) = sut.getKeyPair(publicKey)

        assertEquals(publicKey.keyAsHex, testPublicKey.keyAsHex)
        assertEquals(privateKey.keyAsHex, testPrivateKey.keyAsHex)
    }

    @Test
    fun `ConcatKeys takes two keys and returns a string of the two keys combined`() {
        val (public, private) = sut.getKeyPair(publicKey)
        assertEquals(publicKey.keyAsHex, public.keyAsHex)
        assertEquals(privateKey.keyAsHex, private.keyAsHex)
    }

    @Test
    fun `Stored KeyPair gets removed when using a PublicKey as the tag for removeKeys`() {
        val (public, private) = sut.getKeyPair(publicKey)
        assertEquals(publicKey.keyAsHex, public.keyAsHex)
        assertEquals(privateKey.keyAsHex, private.keyAsHex)

        sut.removeKeys(publicKey.keyAsHex)

        val (publicAfterRemoval, privateAfterRemoval) = sut.getKeyPair(publicKey)
        assertEquals(String.Empty, publicAfterRemoval.keyAsHex)
        assertEquals(String.Empty, privateAfterRemoval.keyAsHex)
    }

    @Test
    fun `Generated SymmetricKey gets removed when using a TopicVO as the tag for removeKeys`() {
        val symKey = sut.generateSymmetricKey(topicVO)

        val secretKey = sut.getSymmetricKey(topicVO)
        assertEquals(symKey.keyAsHex, secretKey.keyAsHex)

        sut.removeKeys(topicVO.value)

        val secretKeyAfterRemoval = sut.getSymmetricKey(topicVO)
        assertEquals(String.Empty, secretKeyAfterRemoval.keyAsHex)
    }
}