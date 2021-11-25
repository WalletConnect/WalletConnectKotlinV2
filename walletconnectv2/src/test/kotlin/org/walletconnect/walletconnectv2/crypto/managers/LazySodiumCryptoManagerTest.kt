package org.walletconnect.walletconnectv2.crypto.managers

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.utils.HexMessageEncoder
import com.goterl.lazysodium.utils.Key
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.PrivateKey
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey
import org.walletconnect.walletconnectv2.storage.KeyChain
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

//TODO when the lib sodium is replaced by java.security package
internal class LazySodiumCryptoManagerTest {
    private val privateKeyString = "BCA8EF78C5D69A3681E87A0630E16AC374B6ED612EDAB1BD26F02C4B2499851E"
    private val publicKeyString = "DC22D30CFB89E30A356BA86EE48F66F1722C9B32CC9C0666A47748376BEC177D"
    private val privateKey = PrivateKey(privateKeyString)
    private val publicKey = PublicKey(publicKeyString)
    private val keyChain: KeyChain = mockk()
    private val lazySodium: LazySodiumAndroid = mockk()

    private val sut = spyk(LazySodiumCryptoManager(keyChain), recordPrivateCalls = true)

    @BeforeEach
    fun setUp() {
        sut.setKeyPair(publicKey, privateKey)
    }

    @Test
    fun `Verify that the generated public key is a valid key`() {
        val publicKey = sut.generateKeyPair()
        val expectedKey = Key.fromHexString(publicKey.keyAsHex)

        assert(publicKey.keyAsHex.length == 64)
        assertEquals(expectedKey.asHexString.lowercase(), publicKey.keyAsHex)
    }

    @Test
    fun `Generate a shared key and return a Topic object`() {
        val peerKey = PublicKey("D083CDBBD08B93BD9AD10E95712DC0D4BD880401B04D587D8D3782FEA0CD31A9")
        val (_, topic) = sut.generateTopicAndSharedKey(publicKey, peerKey)

        assert(topic.topicValue.isNotBlank())
        assert(topic.topicValue.length == 64)
    }

    @Test
    fun `Generate a Topic with a sharedKey and a public key and no existing topic`() {
        val sharedKeyString = SharedKey("D083CDBBD08B93BD9AD10E95712DC0D4BD880401B04D587D8D3782FEA0CD31A9")
        val sharedKey = object : org.walletconnect.walletconnectv2.crypto.data.Key {
            override val keyAsHex: String = sharedKeyString.keyAsHex
        }
        sut.setEncryptionKeys(sharedKeyString, publicKey, Topic("topic"))

        assertEquals(sharedKey.keyAsHex, keyChain.getKeys(Topic("topic").topicValue).first)
        assertEquals(publicKey.keyAsHex, keyChain.getKeys(Topic("topic").topicValue).second)
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

        assertEquals(publicKey, testPublicKey)
        assertEquals(privateKey, testPrivateKey)
    }

    @Test
    fun `ConcatKeys takes two keys and returns a string of the two keys combined`() {
        sut.setKeyPair(publicKey, privateKey)
        val (public, private) = sut.getKeyPair(publicKey)

        assertEquals(publicKeyString, public.keyAsHex)
        assertEquals(privateKeyString, private.keyAsHex)

        assertContentEquals(HexMessageEncoder().decode(public.keyAsHex), publicKeyString.hexStringToByteArray())
    }

    @Test
    fun `Decrypt Payload`() {
        val result = sut.getSharedKeyUsingPrivate(
            PrivateKey("1fb63fca5c6ac731246f2f069d3bc2454345d5208254aa8ea7bffc6d110c8862"),
            PublicKey("590c2c627be7af08597091ff80dd41f7fa28acd10ef7191d7e830e116d3a186a")
        )

        assertEquals(
            "9c87e48e69b33a613907515bcd5b1b4cc10bbaf15167b19804b00f0a9217e607",
            result.lowercase()
        )
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val hexChars = "0123456789ABCDEF"
        val result = ByteArray(length / 2)

        for (i in 0 until length step 2) {
            val firstIndex = hexChars.indexOf(this[i])
            val secondIndex = hexChars.indexOf(this[i + 1])

            val octet = firstIndex.shl(4).or(secondIndex)
            result[i.shr(1)] = octet.toByte()
        }

        return result
    }
}