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
            set(0)
            set(1)
            set(2)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("00000111", binaryString)
    }

    @Test
    fun `test printing BitSet as binary string with all bits false`() {
        val bitset = BitSet()

        val binaryString = bitset.toBinaryString()

        assertEquals("", binaryString)
    }

    @Test
    fun `test printing BitSet as binary string with some bits false and some bits true`() {
        val bitset = BitSet().apply {
            set(0)
            set(1)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("00000011", binaryString)
    }

    @Test
    fun `test printing BitSet as binary string with leading zeros removed`() {
        val bitset = BitSet().apply {
            set(2)
        }

        val binaryString = bitset.toBinaryString().removeLeadingZeros()

        assertEquals("100", binaryString)
    }
}