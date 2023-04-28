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
        const val SIGNATURE = "0xc91265eadb1473d90f8d49d31b7016feb7f7761a2a986ca2146a4b8964f3357569869680154927596a5829ceea925f4196b8a853a29c2c1d5915832fc9f1c6a01c"
    }

    private val prototypeStore = Store(PROTOTYPE_STORE_NAME)

    @Test
    fun `Kotlin cryptography matches Javascript cryptography`() {
        val keyPath = prototypeStore.getDerivationPath()
        val entropy = sha256(SIGNATURE.toByteArray())
        val mnemonic = MnemonicWords(entropyToMnemonic(entropy.hexToBytes(), WORDLIST_ENGLISH))
        val privKey = mnemonic.toKey(keyPath).keyPair.privateKey.key.toByteArray().bytesToHex()
        val topic = sha256(privKey.hexToBytes())

        assertEquals("118cf02858a7e588b0e76f40bcfd7dd985eb3a21dc524b0200a7cbcbec0a7841", entropy)
        assertEquals("m/77'/0'/0/1836658037/1936028205/1886547814/6909029", keyPath)
        assertEquals("164a1b53452729c86d18127d912a9bab83c516885101a0b0fb8287b998014e74", privKey)
        assertEquals(
            "bag guide anxiety rally layer session seminar unknown doll tree garage reason gadget other manual medal enrich avoid clarify nurse salute ahead three caution",
            mnemonic.toString()
        )
        assertEquals("741f8902d339c4c16f33fa598a6598b63e5ed125d761374511b2e06562b033eb", topic)
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
