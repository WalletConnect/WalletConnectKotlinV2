package com.walletconnect.android.internal

import com.walletconnect.utils.removeLeadingZeros
import com.walletconnect.utils.toBinaryString
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class UserAgentFormattingTest {

    @Test
    fun `test printing BitSet as binary string with 3 bits true`() {
        val bitset = BitSet().apply {
            set(0, true)
            set(1, true)
            set(2, true)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("00000111", binaryString)
    }

    @Test
    fun `test printing BitSet as binary string with 3 bits false`() {
        val bitset = BitSet().apply {
            set(0, false)
            set(1, false)
            set(2, false)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("", binaryString)
    }

    @Test
    fun `test printing BitSet as binary string with some bits false and some bits true`() {
        val bitset = BitSet().apply {
            set(0, true)
            set(1, true)
            set(2, false)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("00000011", binaryString)
    }

    @Test
    fun `test printing BitSet as binary string with leading zeros removed`() {
        val bitset = BitSet().apply {
            set(0, false)
            set(1, false)
            set(2, true)
            set(3, false)
            set(4, false)
        }

        val binaryString = bitset.toBinaryString().removeLeadingZeros()

        assertEquals("100", binaryString)
    }
}