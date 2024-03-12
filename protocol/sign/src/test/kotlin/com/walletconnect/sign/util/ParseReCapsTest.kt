package com.walletconnect.sign.util

import com.walletconnect.android.internal.common.signing.cacao.getChains
import com.walletconnect.android.internal.common.signing.cacao.getMethods
import org.bouncycastle.util.encoders.Base64
import org.junit.Test

class ParseReCapsTest {

    @Test
    fun decodeReCapsTest() {
        val resources =
            listOf(
                "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ==",
                "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX0sImh0dHBzOi8vbm90aWZ5LndhbGxldGNvbm5lY3QuY29tL2FsbC1hcHBzIjp7ImNydWQvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZC9ub3RpZmljYXRpb25zIjpbe31dfX19"
            )

        val chains = resources.getChains()
        val actions = resources.getMethods()

        assert(chains == listOf("eip155:1"))
        assert(actions == listOf("eth_signTypedData_v4", "personal_sign"))
    }

    @Test
    fun parseReCapsTest() {
        val urn1 =
            Base64.toBase64String("""{"att":{"eip155":{"request/personal_sign":[{"chains": ["eip155:1", "eip155:137"]}],"request/eth_sign":[{"chains": ["eip155:1"]}]}}}""".toByteArray(Charsets.UTF_8))
        val urn2 =
            Base64.toBase64String("""{"att":{"test":{"different_action":[{"diff_chains": ["eip155:1"]}],"yet_another_action":[{"yet_another_chains": ["eip155:1"]}]}}}""".toByteArray(Charsets.UTF_8))
        val jsonList = mutableListOf("urn:recap:$urn2", "urn:recap:$urn1")
        val chains = jsonList.getChains()
        val actions = jsonList.getMethods()

        assert(chains == listOf("eip155:1", "eip155:137"))
        assert(actions == listOf("eth_sign", "personal_sign"))
    }

    @Test
    fun parseReCapsFromIOSTest() {
        val urn1 = Base64.toBase64String("""{"att":{"eip155":{"request\/personal_sign":[{"chains":["eip155:1","eip155:137"]}]}}}""".toByteArray(Charsets.UTF_8))
        val jsonList = mutableListOf("urn:recap:$urn1")
        val chains = jsonList.getChains()
        val actions = jsonList.getMethods()

        assert(chains == listOf("eip155:1", "eip155:137"))
        assert(actions == listOf("personal_sign"))
    }
}