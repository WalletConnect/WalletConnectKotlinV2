package com.walletconnect.sign.util

import com.walletconnect.android.internal.common.signing.cacao.getActions
import com.walletconnect.android.internal.common.signing.cacao.getChains
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.utils.generateAuthPayloadParams
import org.junit.Test

class GenerateAuthPayloadParamsTest {
    //eth_signTypedData_v4 + personal_sign
    private val encodedSignRecaps =
        "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ=="
    private val encodedNotifyAndSignRecaps =
        "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX0sImh0dHBzOi8vbm90aWZ5LndhbGxldGNvbm5lY3QuY29tL2FsbC1hcHBzIjp7ImNydWQvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZC9ub3RpZmljYXRpb25zIjpbe31dfX19"

    @Test
    fun testHandlingSupportedChainsAndMethodsInReCaps() {
        val requestedPayload = Sign.Model.PayloadParams(
            chains = listOf("eip155:1"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            iat = "iat",
            statement = null,
            requestId = null,
            resources = listOf("test_resource", encodedSignRecaps)
        )
        val result = generateAuthPayloadParams(requestedPayload, listOf("eip155:1"), listOf("eth_signTypedData_v4", "personal_sign"))
        val sessionChains = result.resources.getChains().ifEmpty { requestedPayload.chains }
        val sessionMethods = result.resources.getActions()

        assert(sessionMethods == listOf("personal_sign", "eth_signTypedData_v4"))
        assert(sessionChains == listOf("eip155:1"))
    }

    @Test
    fun test() {
        val requestedPayload = Sign.Model.PayloadParams(
            chains = listOf("eip155:1"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            iat = "iat",
            statement = null,
            requestId = null,
            resources = listOf("test_resource", encodedSignRecaps)
        )
        val result = generateAuthPayloadParams(requestedPayload, listOf("eip155:333"), listOf("not_supported"))
        val sessionChains = result.resources.getChains().ifEmpty { requestedPayload.chains }
        val sessionMethods = result.resources.getActions()

        assert(sessionMethods == listOf("personal_sign", "eth_signTypedData_v4"))
        assert(sessionChains == listOf("eip155:1"))
    }

    @Test
    fun testHandlingAddingMoreThatRequestedChainsAndMethodsInReCaps() {
        val requestedPayload = Sign.Model.PayloadParams(
            chains = listOf("eip155:1"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            iat = "iat",
            statement = null,
            requestId = null,
            resources = listOf("test_resource", encodedSignRecaps)
        )
        val result =
            generateAuthPayloadParams(requestedPayload, supportedChains = listOf("eip155:1", "eip155:137", "eip155:56"), supportedMethods = listOf("eth_signTypedData_v4", "personal_sign", "eth_sign"))
        val sessionChains = result.resources.getChains().ifEmpty { requestedPayload.chains }
        val sessionMethods = result.resources.getActions()

        assert(sessionMethods == listOf("personal_sign", "eth_signTypedData_v4"))
        assert(sessionChains == listOf("eip155:1"))
    }

    @Test
    fun testHandlingAddingLessThatRequestedChainsAndMethodsInReCaps() {
        val requestedPayload = Sign.Model.PayloadParams(
            chains = listOf("eip155:1", "eip155:137", "eip155:56"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            iat = "iat",
            statement = null,
            requestId = null,
            resources = listOf("test_resource", encodedSignRecaps)
        )
        val result =
            generateAuthPayloadParams(requestedPayload, supportedChains = listOf("eip155:137", "eip155:56"), supportedMethods = listOf("personal_sign"))
        val sessionChains = result.resources.getChains().ifEmpty { requestedPayload.chains }
        val sessionMethods = result.resources.getActions()

        assert(sessionMethods == listOf("personal_sign"))
        assert(sessionChains == listOf("eip155:137", "eip155:56"))
    }

    @Test
    fun testHandlingSupportedRequestedChainsAndMethodsInTwoReCapsWithOneUrns() {
        val requestedPayload = Sign.Model.PayloadParams(
            chains = listOf("eip155:1", "eip155:137", "eip155:56"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            iat = "iat",
            statement = null,
            requestId = null,
            resources = listOf("test_resource", encodedNotifyAndSignRecaps)
        )
        val result =
            generateAuthPayloadParams(requestedPayload, supportedChains = listOf("eip155:137", "eip155:56"), supportedMethods = listOf("personal_sign", "eth_signTypedData_v4"))
        val sessionChains = result.resources.getChains().ifEmpty { requestedPayload.chains }
        val sessionMethods = result.resources.getActions()

        assert(sessionMethods == listOf("personal_sign", "eth_signTypedData_v4"))
        assert(sessionChains == listOf("eip155:137", "eip155:56"))
    }

    @Test
    fun testHandlingSupportedRequestedChainsAndMethodsInOneReCapsWithTwoUrns() {
        val requestedPayload = Sign.Model.PayloadParams(
            chains = listOf("eip155:1", "eip155:137", "eip155:56"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            iat = "iat",
            statement = null,
            requestId = null,
            resources = listOf("test_resource", encodedNotifyAndSignRecaps, encodedSignRecaps)
        )
        val result =
            generateAuthPayloadParams(requestedPayload, supportedChains = listOf("eip155:137"), supportedMethods = listOf("personal_sign"))
        val sessionChains = result.resources.getChains().ifEmpty { requestedPayload.chains }
        val sessionMethods = result.resources.getActions()

        assert(sessionMethods == listOf("personal_sign"))
        assert(sessionChains == listOf("eip155:137"))
    }
}