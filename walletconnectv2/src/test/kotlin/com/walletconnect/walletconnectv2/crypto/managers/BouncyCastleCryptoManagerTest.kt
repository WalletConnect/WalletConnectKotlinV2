package com.walletconnect.walletconnectv2.crypto.managers

import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.Key
import com.walletconnect.walletconnectv2.common.model.vo.PrivateKey
import com.walletconnect.walletconnectv2.common.model.vo.PublicKey
import com.walletconnect.walletconnectv2.common.model.vo.SharedKey
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.crypto.data.repository.BouncyCastleCryptoRepository
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class BouncyCastleCryptoManagerTest {
    private val privateKeyString = "BCA8EF78C5D69A3681E87A0630E16AC374B6ED612EDAB1BD26F02C4B2499851E".lowercase()
    private val publicKeyString = "DC22D30CFB89E30A356BA86EE48F66F1722C9B32CC9C0666A47748376BEC177D".lowercase()
    private val privateKey = PrivateKey(privateKeyString)
    private val publicKey = PublicKey(publicKeyString)
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

        assertEquals(
            "9c87e48e69b33a613907515bcd5b1b4cc10bbaf15167b19804b00f0a9217e607",
            result.lowercase()
        )
    }


    @Test
    fun `Generate a shared key and return a Topic object`() {
        val peerKey = PublicKey("D083CDBBD08B93BD9AD10E95712DC0D4BD880401B04D587D8D3782FEA0CD31A9".lowercase())

        val (sharedKey, topic) = sut.generateTopicAndSharedKey(publicKey, peerKey)

        assert(topic.value.isNotBlank())
        assert(topic.value.length == 64)
        assert(sharedKey.keyAsHex.length == 64)

        assertEquals(topic.value, "7c3adceb58cce0035cb5f4100b5980000dbba4a920b1da1568377fc4e2c3ab2b")
        assertEquals(sharedKey.keyAsHex, "af2be9138502d5a2127e691670993e4007337236e9d182fdcf654f2b5bee2038")
    }

    @Test
    fun `Generate a Topic with a sharedKey and a public key and no existing topic`() {
        val sharedKeyString = SharedKey("D083CDBBD08B93BD9AD10E95712DC0D4BD880401B04D587D8D3782FEA0CD31A9".lowercase())
        val sharedKey = object : Key {
            override val keyAsHex: String = sharedKeyString.keyAsHex
        }
        sut.setEncryptionKeys(sharedKeyString, publicKey, TopicVO("topic"))

        assertEquals(sharedKey.keyAsHex, keyChain.getKeys(TopicVO("topic").value).first)
        assertEquals(publicKey.keyAsHex, keyChain.getKeys(TopicVO("topic").value).second)
    }

    @Test
    fun `SetKeyPair sets the concatenated keys to storage`() {
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
        sut.setKeyPair(publicKey, privateKey)
        val (public, private) = sut.getKeyPair(publicKey)

        assertEquals(publicKeyString, public.keyAsHex)
        assertEquals(privateKeyString, private.keyAsHex)
    }
}