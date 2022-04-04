package com.walletconnect.walletconnectv2.crypto.managers

import com.walletconnect.walletconnectv2.core.model.vo.PrivateKey
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.crypto.data.repository.BouncyCastleCryptoRepository
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class BouncyCastleCryptoManagerTest {
    private val publicKey = PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a")
    private val privateKey = PrivateKey("36bf507903537de91f5e573666eaa69b1fa313974f23b2b59645f20fea505854")
    private val keyChain: KeyStore = KeyChainMock()
    private val sut = spyk(BouncyCastleCryptoRepository(keyChain), recordPrivateCalls = true)

    @BeforeEach
    fun setUp() {
        sut.setKeyPair(publicKey, privateKey)
    }

    @Test
    fun `Verify that the generated public key has valid length`() {
        val publicKey = sut.generateKeyPair()

        val test = "7d819cb5192e0f18aa4394b72d24d2a02df773a0eb56a990e9adcdb16db39e7b"
        assert(test.length == 64)
        assert(publicKey.keyAsHex.length == 64)
    }

    @Test
    fun `Generate shared key test`() {
        val result = sut.getSharedKey(
            PrivateKey("1fb63fca5c6ac731246f2f069d3bc2454345d5208254aa8ea7bffc6d110c8862"),
            PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a")
        )

        assert(result.length == 64)
        assertEquals("9c87e48e69b33a613907515bcd5b1b4cc10bbaf15167b19804b00f0a9217e607", result.lowercase())
    }

    @Test
    fun `generate symmetric key test`() {
        val topic = TopicVO("topic")
        val symKey = sut.generateSymmetricKey(topic)
        assert(symKey.keyAsHex.length == 64)

        val secretKey = sut.getSecretKey(topic)

        assertEquals(symKey.keyAsHex, secretKey.keyAsHex)
        assert(secretKey.keyAsHex.length == 64)
    }

    @Test
    fun `Generate a shared key and return a Topic object`() {
        val peerKey = PublicKey("ff7a7d5767c362b0a17ad92299ebdb7831dcbd9a56959c01368c7404543b3342")

        val (sharedKey, topic) =
            sut.generateTopicAndSharedKey(PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a"), peerKey)

        assert(topic.value.isNotBlank())
        assert(topic.value.length == 64)
        assert(sharedKey.keyAsHex.length == 64)

        assertEquals("2c03712132ad2f85adc472a2242e608d67bfecd4362d05012d69a89143fecd16", topic.value)
        assertEquals("0653ca620c7b4990392e1c53c4a51c14a2840cd20f0f1524cf435b17b6fe988c", sharedKey.keyAsHex)
    }

    @Test
    fun `SetKeyPair sets the concatenated keys to storage`() {
        val publicKeyString = "590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a"
        val privateKeyString = "36bf507903537de91f5e573666eaa69b1fa313974f23b2b59645f20fea505854"
        sut.setKeyPair(publicKey, privateKey)

        assertNotNull(sut.getKeyPair(publicKey))
        assertEquals(publicKeyString, keyChain.getKeys(publicKeyString).first)
        assertEquals(privateKeyString, keyChain.getKeys(publicKeyString).second)
    }

    @Test
    fun `GetKeyPair gets a pair of PublicKey and PrivateKey when using a PublicKey as the key`() {
        val (testPublicKey, testPrivateKey) = sut.getKeyPair(publicKey)

        assertEquals(publicKey.keyAsHex, testPublicKey.keyAsHex)
        assertEquals(privateKey.keyAsHex, testPrivateKey.keyAsHex)
    }

    @Test
    fun `ConcatKeys takes two keys and returns a string of the two keys combined`() {
        val publicKeyString = "590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a"
        val privateKeyString = "36bf507903537de91f5e573666eaa69b1fa313974f23b2b59645f20fea505854"
        sut.setKeyPair(publicKey, privateKey)

        val (public, private) = sut.getKeyPair(publicKey)
        assertEquals(publicKeyString, public.keyAsHex)
        assertEquals(privateKeyString, private.keyAsHex)
    }
}