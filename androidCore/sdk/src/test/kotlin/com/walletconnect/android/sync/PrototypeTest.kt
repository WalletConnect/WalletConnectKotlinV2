package com.walletconnect.android.sync

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.bytesToInt
import com.walletconnect.util.hexToBytes
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.kethereum.bip39.entropyToMnemonic
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toKey
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH

// Based on javascript prototype test https://github.com/WalletConnect/sync-api-prototype/blob/main/src/index.ts

class PrototypeTest {

    companion object {
        const val PROTOTYPE_STORE_NAME = "my-user-profile"
        const val SIGNATURE = "0xee6567bf0763ce704d4cc3ec919cb74bbb484222e19ad72f51072fbdc2af7add063c00ac334a510c51fd25daf14f87337c23a81d45ac4f1dde469a0d8dc5724b1b"
    }

    private val prototypeStore = Store(PROTOTYPE_STORE_NAME)

    @Test
    fun `Kotlin cryptography matches Javascript cryptography`() {
        val keyPath = prototypeStore.getDerivationPath()
        val entropy = sha256(SIGNATURE.toByteArray())
        val mnemonic = MnemonicWords(entropyToMnemonic(entropy.hexToBytes(), WORDLIST_ENGLISH))
        val privKey = mnemonic.toKey(keyPath).keyPair.privateKey.key.toByteArray().bytesToHex().takeLast(64)
        val topic = sha256(privKey.hexToBytes())

        assertEquals("98363a603bb3aeb12b2a1686e54190822ca39ba6593aa512679630ee42f77dc4", entropy)
        assertEquals("m/77'/0'/0/1836658037/1936028205/1886547814/6909029", keyPath)
        assertEquals("02fe412cf77b84f7e1dcac2ac036ba5da857ef6c683e6e93a39005734cb289f4", privKey)
        assertEquals("7a73cffc9951264511549e64222a612a27199b01d30fa952b708bcafce96ea3f", topic)
    }

    @Test
    fun `storeToPath matches js implementation`() {
        val jsResult = "1836658037/1936028205/1886547814/6909029"
        assertEquals(jsResult, prototypeStore.toPath())
    }

    @Test
    fun `ByteArray of 4 to Int test`() {
        // Average conversion

        val value = byteArrayOf(0x12, 0x34, 0x56, 0x78).bytesToInt(4) //0x12345678 -> 305419896
        assertEquals(305419896, value)

        // Handling zeros and missing values

        val value2 = byteArrayOf(0x12, 0x34, 0x56, 0x00).bytesToInt(4) //0x12345600 -> 305419776
        assertEquals(305419776, value2)

        val value3 = byteArrayOf(0x12, 0x34, 0x56).bytesToInt(3) //0x00123456 -> 1193046
        assertEquals(1193046, value3)

        val value4 = byteArrayOf(0x00, 0x12, 0x34, 0x56).bytesToInt(4) //0x00123456 -> 1193046
        assertEquals(1193046, value4)
    }
}
