package com.walletconnect.android.sync

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.bytesToInt
import com.walletconnect.util.hexToBytes
import org.junit.jupiter.api.Test
import org.kethereum.bip39.entropyToMnemonic
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toKey
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH
import kotlin.test.assertEquals

// Based on javascript prototype test https://github.com/WalletConnect/sync-api-prototype/blob/main/src/index.ts

class PrototypeTest {

    companion object {
        const val PROTOTYPE_STORE_NAME = "my-user-profile"
        const val SIGNATURE = "0xfbbdc863d63c02692dc20bf61415dbd68e531882fd5725c4349df4f2e294307b2b5acc8751e12b1bf65e4c35128fa917a6200e3855c12b5e967fe2b26dc9f6031c"
    }

    private val prototypeStore = Store(PROTOTYPE_STORE_NAME)

    @Test
    fun `Kotlin cryptography matches Javascript cryptography`() {
        val keyPath = prototypeStore.getDerivationPath()
        val entropy = sha256(SIGNATURE.toByteArray())
        val mnemonic = MnemonicWords(entropyToMnemonic(entropy.hexToBytes(), WORDLIST_ENGLISH))
        val privKey = mnemonic.toKey(keyPath).keyPair.privateKey.key.toByteArray().bytesToHex().takeLast(64)
        val topic = sha256(privKey.hexToBytes())

        assertEquals("0a0801cec7d99e78ed8c9a9bfda87bdf7f59e93b377be4e4eb58883be943668a", entropy)
        assertEquals("m/77'/0'/0/1836658037/1936028205/1886547814/6909029", keyPath)
        assertEquals("3b17151f7bb5d4421e0c647f2b59eae81e4bf3a5458a3bbd8169d8a70132bbaf", privKey)
        assertEquals("9a07815209f63b80e9af08e5922e70802089989aee3b991a102cf28efd4b984f", topic)
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
