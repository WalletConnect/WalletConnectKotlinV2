package com.walletconnect.auth.signature

import com.walletconnect.auth.common.model.Issuer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class IssuerTest {
    @Test
    fun getChainIdAndAccountIdFromIssuerTest() {
        val iss = Issuer("did:pkh:eip155:1:0x46586f7F766955CAF22A54dDA7570E6eFA94c16c")
        assertEquals("1", iss.chainIdReference)
        assertEquals("eip155:1", iss.chainId)
        assertEquals("0x46586f7F766955CAF22A54dDA7570E6eFA94c16c", iss.address)
        assertEquals("eip155:1:0x46586f7F766955CAF22A54dDA7570E6eFA94c16c", iss.accountId)
    }

    @Test
    fun getChainIdAndAccountIdFromInvalidIssuerTest() {
        val iss = Issuer("did:pkh:0x46586f7F766955CAF22A54dDA7570E6eFA94c16c")
        assertThrows<java.lang.IndexOutOfBoundsException> { iss.address }
        assertThrows<java.lang.IndexOutOfBoundsException> { iss.chainIdReference }
    }
}