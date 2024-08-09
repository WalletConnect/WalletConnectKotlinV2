package com.walletconnect.sign.engine.use_case.calls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GetNamespacesFromReCapsTest {

    @Test
    fun `throw when chains are not caip-2 compliant`() {
        val exception = assertThrows(Exception::class.java) {
            GetNamespacesFromReCaps().invoke(listOf("1", "2"), listOf("eth_sign"))
        }

        assertEquals("Chains are not CAIP-2 compliant", exception.message)
    }

    @Test
    fun `throw when namespace is not eip155`() {
        val exception = assertThrows(Exception::class.java) {
            GetNamespacesFromReCaps().invoke(listOf("polka:2"), listOf("eth_sign"))
        }

        assertEquals("Only eip155 (EVM) is supported", exception.message)
    }

    @Test
    fun `get namespaces successfully`() {
        val namespaces = GetNamespacesFromReCaps().invoke(listOf("eip155:1"), listOf("eth_sign"))

        assertEquals(1, namespaces.size)
        assertEquals("eip155:1", namespaces["eip155"]?.chains?.first())
    }
}