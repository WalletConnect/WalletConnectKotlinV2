package com.walletconnect.android.internal.common.cacao

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.toCAIP122Message
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MapperTest {
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val chainName = "Ethereum"
    private val dummyPairing = Core.Model.Pairing("", 0L, null, "", null, "", true, "")

    @Test
    fun `Payload required fields formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = null,
            requestId = null,
            resources = null
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }

    @Test
    fun `Payload resources formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = null,
            requestId = null,
            resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/", "https://example.com/my-web2-claim.json")
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }

    @Test
    fun `Payload requestId formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = null,
            requestId = "someRequestId",
            resources = null
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Request ID: someRequestId"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }

    @Test
    fun `Payload statement formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = "someStatement",
            requestId = null,
            resources = null
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "someStatement\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }

    @Test
    fun `Payload expiry formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = "2021-09-31T16:25:24Z",
            statement = null,
            requestId = null,
            resources = null
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Expiration Time: 2021-09-31T16:25:24Z"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }

    @Test
    fun `Payload not before formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = "2021-09-31T16:25:24Z",
            exp = null,
            statement = null,
            requestId = null,
            resources = null
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Not Before: 2021-09-31T16:25:24Z"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }

    @Test
    fun `Payload all fields formatting`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = "2021-09-31T16:25:24Z",
            exp = "2021-10-30T16:25:24Z",
            statement = "someStatement",
            requestId = "someRequestId",
            resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/", "https://example.com/my-web2-claim.json")
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "someStatement\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Expiration Time: 2021-10-30T16:25:24Z\n" +
                "Not Before: 2021-09-31T16:25:24Z\n" +
                "Request ID: someRequestId\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json"

        assertEquals(message, payload.toCAIP122Message(chainName))
    }
}