package com.walletconnect.walletconnectv2.crypto.data.repository

import com.walletconnect.walletconnectv2.core.model.vo.PrivateKey
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.crypto.managers.KeyChainMock
import com.walletconnect.walletconnectv2.util.Empty
import io.mockk.spyk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class BouncyCastleCryptoRepositoryTest {
    private val publicKey = PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a")
    private val privateKey = PrivateKey("36bf507903537de91f5e573666eaa69b1fa313974f23b2b59645f20fea505854")
    private val keyChain: KeyStore = KeyChainMock()
    private val sut = spyk(BouncyCastleCryptoRepository(keyChain), recordPrivateCalls = true)
    private val topicVO = TopicVO("topic")

    @Test
    fun `Verify that the generated public key has valid length`() {
        val publicKey = sut.generateKeyPair()
        assert(publicKey.keyAsHex.length == 64)
    }

    @Test
    fun `Generate shared key test`() {
        val result = sut.getSharedKey(
            PrivateKey("1fb63fca5c6ac731246f2f069d3bc2454345d5208254aa8ea7bffc6d110c8862"),
            publicKey
        )

        assert(result.length == 64)
        assertEquals("9c87e48e69b33a613907515bcd5b1b4cc10bbaf15167b19804b00f0a9217e607", result.lowercase())
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
        sut.setKeyPair(publicKey, privateKey)
        val peerKey = PublicKey("ff7a7d5767c362b0a17ad92299ebdb7831dcbd9a56959c01368c7404543b3342")
        val (sharedKey, topic) = sut.generateTopicAndSharedKey(publicKey, peerKey)

        assert(topic.value.isNotBlank())
        assert(topic.value.length == 64)
        assert(sharedKey.keyAsHex.length == 64)

        assertEquals("2c03712132ad2f85adc472a2242e608d67bfecd4362d05012d69a89143fecd16", topic.value)
        assertEquals("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c", sharedKey.keyAsHex)
    }

    @Test
    fun `SetKeyPair sets the concatenated keys to storage`() {
        sut.setKeyPair(publicKey, privateKey)

        assertNotNull(sut.getKeyPair(publicKey))
        assertEquals(publicKey.keyAsHex, keyChain.getKeys(publicKey.keyAsHex).first)
        assertEquals(privateKey.keyAsHex, keyChain.getKeys(publicKey.keyAsHex).second)
    }

    @Test
    fun `GetKeyPair gets a pair of PublicKey and PrivateKey when using a PublicKey as the key`() {
        sut.setKeyPair(publicKey, privateKey)
        val (testPublicKey, testPrivateKey) = sut.getKeyPair(publicKey)

        assertEquals(publicKey.keyAsHex, testPublicKey.keyAsHex)
        assertEquals(privateKey.keyAsHex, testPrivateKey.keyAsHex)
    }

    @Test
    fun `ConcatKeys takes two keys and returns a string of the two keys combined`() {
        sut.setKeyPair(publicKey, privateKey)

        val (public, private) = sut.getKeyPair(publicKey)
        assertEquals(publicKey.keyAsHex, public.keyAsHex)
        assertEquals(privateKey.keyAsHex, private.keyAsHex)
    }

    @Test
    fun `Stored KeyPair gets removed when using a PublicKey as the tag for removeKeys`() {
        sut.setKeyPair(publicKey, privateKey)

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

        val secretKey = sut.getSecretKey(topicVO)
        assertEquals(symKey.keyAsHex, secretKey.keyAsHex)

        sut.removeKeys(topicVO.value)

        val secretKeyAfterRemoval = sut.getSecretKey(topicVO)
        assertEquals(String.Empty, secretKeyAfterRemoval.keyAsHex)
    }
}