package org.walletconnect.walletconnectv2.crypto.managers

import com.goterl.lazysodium.utils.HexMessageEncoder
import com.goterl.lazysodium.utils.Key
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.crypto.data.PrivateKey
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class LazySodiumCryptoManagerTest {
    private val privateKeyString = "BCA8EF78C5D69A3681E87A0630E16AC374B6ED612EDAB1BD26F02C4B2499851E"
    private val publicKeyString = "DC22D30CFB89E30A356BA86EE48F66F1722C9B32CC9C0666A47748376BEC177D"
    private val privateKey = PrivateKey(privateKeyString)
    private val publicKey = PublicKey(publicKeyString)
    private val keyChain = object: KeyChain {
        val mapOfKeys = mutableMapOf<String, String>()

        override fun setKey(key: String, value: String) {
            mapOfKeys[key] = value
        }

        override fun getKey(key: String): String {
            return mapOfKeys[key]!!
        }
    }
    private val sut = spyk(LazySodiumCryptoManager(keyChain), recordPrivateCalls = true)

    @BeforeEach
    fun setUp() {
        keyChain.setKey(publicKeyString, sut.concatKeys(publicKey, privateKey))
    }

    @Test
    fun `Verify that the generated public key is a valid key`() {
        val publicKey = sut.generateKeyPair()
        val expectedKey = Key.fromHexString(publicKey.key)

        assert(publicKey.key.length == 64)
        assertContentEquals(expectedKey.asBytes, publicKey.key.hexStringToByteArray())
    }

    @Test
    fun `Generate a shared key and return a Topic object`() {
        val peerKey = PublicKey("D083CDBBD08B93BD9AD10E95712DC0D4BD880401B04D587D8D3782FEA0CD31A9")

        val topic = sut.generateSharedKey(publicKey, peerKey, null)

        assert(topic.topicValue.isNotBlank())
        assert(topic.topicValue.length == 64)
    }

    @Test
    fun `Generate a Topic with a sharedKey and a public key and no existing topic`() {
        val sharedKeyString = "D083CDBBD08B93BD9AD10E95712DC0D4BD880401B04D587D8D3782FEA0CD31A9"
        val sharedKey = object: org.walletconnect.walletconnectv2.crypto.data.Key {
            override val key: String = sharedKeyString
        }
        val topic = sut.setEncryptionKeys(sharedKeyString, publicKey, null)

        assertEquals(sut.concatKeys(sharedKey, publicKey), keyChain.getKey(topic.topicValue))
    }

    @Test
    fun `SetKeyPair sets the concatenated keys to storage`() {
        sut.setKeyPair(publicKey, privateKey)

        assertNotNull(keyChain.mapOfKeys[publicKeyString])
        assertEquals(publicKeyString + privateKeyString, keyChain.mapOfKeys[publicKeyString])

        verify {
            sut.concatKeys(publicKey, privateKey)
        }
    }

    @Test
    fun `GetKeyPair gets a pair of PublicKey and PrivateKey when using a PublicKey as the key`() {
        val (testPublicKey, testPrivateKey) = sut.getKeyPair(publicKey)

        assertEquals(publicKey, testPublicKey)
        assertEquals(privateKey, testPrivateKey)
    }

    @Test
    fun `ConcatKeys takes two keys and returns a string of the two keys combined`() {
        val concatString = sut.concatKeys(publicKey, privateKey)

        assertEquals(publicKeyString + privateKeyString, concatString)
        assertContentEquals(HexMessageEncoder().decode(concatString), concatString.hexStringToByteArray())
    }

    @Test
    fun `Split a concatenated key into a pair of keys`() {
        val concatKeys = sut.concatKeys(publicKey, privateKey)

        val (splitPublicKey, splitPrivateKey) = sut.splitKeys(concatKeys)

        assertEquals(publicKeyString, splitPublicKey.key)
        assertEquals(privateKeyString, splitPrivateKey.key)
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val hexChars = "0123456789ABCDEF"
        val result = ByteArray(length / 2)

        for (i in 0 until length step 2) {
            val firstIndex = hexChars.indexOf(this[i]);
            val secondIndex = hexChars.indexOf(this[i + 1]);

            val octet = firstIndex.shl(4).or(secondIndex)
            result[i.shr(1)] = octet.toByte()
        }

        return result
    }
}