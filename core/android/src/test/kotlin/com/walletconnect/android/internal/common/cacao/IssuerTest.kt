package com.walletconnect.android.internal.common.cacao

import com.walletconnect.android.internal.common.signing.cacao.Issuer
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { iss.address }
        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { iss.chainIdReference }
    }
}