package com.walletconnect.android.internal

import com.walletconnect.utils.combineListOfBitSetsWithOrOperator
import com.walletconnect.utils.removeLeadingZeros
import com.walletconnect.utils.toBinaryString
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.BitSet

class UserAgentFormattingTest {

    @Test
    fun `printing BitSet as binary string with 3 bits true`() {
        val bitset = BitSet().apply {
            set(0)
            set(1)
            set(2)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("00000111", binaryString)
    }

    @Test
    fun `printing BitSet as binary string with all bits false`() {
        val bitset = BitSet()

        val binaryString = bitset.toBinaryString()

        assertEquals("", binaryString)
    }

    @Test
    fun `printing BitSet as binary string with some bits false and some bits true`() {
        val bitset = BitSet().apply {
            set(0)
            set(1)
        }

        val binaryString = bitset.toBinaryString()

        assertEquals("00000011", binaryString)
    }

    @Test
    fun `printing BitSet as binary string with leading zeros removed`() {
        val bitset = BitSet().apply {
            set(2)
        }

        val binaryString = bitset.toBinaryString().removeLeadingZeros()

        assertEquals("100", binaryString)
    }

    @Test
    fun `combing a BitSet with another BitSet using the or operator`() {
        val bitset1 = BitSet().apply {
            set(0)
            set(1)
        }
        val bitset2 = BitSet().apply {
            set(2)
            set(7)
        }
        val combinedBitset = BitSet()

        combinedBitset.or(bitset1)
        combinedBitset.or(bitset2)

        assertEquals("00000011", bitset1.toBinaryString())
        assertEquals("10000100", bitset2.toBinaryString())
        assertEquals("10000111", combinedBitset.toBinaryString())
    }

    @Test
    fun `reducing a list of BitSets using or`() {
        val bitset1 = BitSet().apply {
            set(0)
            set(1)
        }
        val bitset2 = BitSet().apply {
            set(2)
            set(7)
        }
        val bitset3 = BitSet().apply {
            set(3)
            set(4)
        }
        val emptyRootBitset = BitSet()
        val combinedBitset = combineListOfBitSetsWithOrOperator(listOf(emptyRootBitset, bitset1, bitset2, bitset3))

        assertEquals("00000011", bitset1.toBinaryString())
        assertEquals("10000100", bitset2.toBinaryString())
        assertEquals("00011000", bitset3.toBinaryString())
        assertEquals("10011111", combinedBitset.toBinaryString())
    }

    @Test
    fun `reducing a list empty BitSets should throw`() {
        assertThrows(UnsupportedOperationException::class.java) {
            combineListOfBitSetsWithOrOperator(emptyList())
        }
    }
}