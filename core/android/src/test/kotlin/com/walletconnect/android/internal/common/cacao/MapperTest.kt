package com.walletconnect.android.internal.common.cacao

import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.toCAIP222Message
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class MapperTest {
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val chainName = "Ethereum"
    private val encodedSignRecaps = "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3t9XX19fQ=="
    private val encodedNotifyRecaps = "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0="
    private val encodedNotifyAndSignRecaps =
        "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3t9XX0sImh0dHBzOi8vbm90aWZ5LndhbGxldGNvbm5lY3QuY29tL2FsbC1hcHBzIjp7ImNydWQvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZC9ub3RpZmljYXRpb25zIjpbe31dfX19"

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

        assertEquals(message, payload.toCAIP222Message(chainName))
    }

    @Test
    fun `Test formatting CAIP-222 message with Sign ReCaps`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = "Statement",
            requestId = null,
            resources = listOf(
                "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
                "https://example.com/my-web2-claim.json",
                encodedSignRecaps
            )
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "Statement I further authorize the stated URI to perform the following actions on my behalf: (1) 'request': 'eth_signTypedData_v4', 'personal_sign' for 'eip155'\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json\n" +
                "- $encodedSignRecaps"

        assertEquals(message, payload.toCAIP222Message(chainName))
    }

    @Test
    fun `Test formatting CAIP-222 message with Notify ReCaps`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = "Statement",
            requestId = null,
            resources = listOf(
                "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
                "https://example.com/my-web2-claim.json",
                encodedNotifyRecaps
            )
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "Statement I further authorize the stated URI to perform the following actions on my behalf: (1) 'crud': 'notifications', 'subscriptions' for 'https://notify.walletconnect.com/all-apps'\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json\n" +
                "- $encodedNotifyRecaps"

        assertEquals(message, payload.toCAIP222Message(chainName))
    }

    @Test
    fun `Test formatting CAIP-222 message with Notify ReCaps and Sign ReCaps`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = "Statement",
            requestId = null,
            resources = listOf(
                "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
                "https://example.com/my-web2-claim.json",
                encodedNotifyRecaps,
                encodedSignRecaps
            )
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "Statement I further authorize the stated URI to perform the following actions on my behalf: (1) 'request': 'eth_signTypedData_v4', 'personal_sign' for 'eip155', (2) 'crud': 'notifications', 'subscriptions' for 'https://notify.walletconnect.com/all-apps'\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json\n" +
                "- $encodedNotifyRecaps\n" +
                "- $encodedSignRecaps"

        assertEquals(message, payload.toCAIP222Message(chainName))
    }

    @Test
    fun `Test formatting CAIP-222 message with Notify ReCaps and Sign ReCaps in one URN`() {
        val payload = Cacao.Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = null,
            exp = null,
            statement = "Statement",
            requestId = null,
            resources = listOf(
                "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
                "https://example.com/my-web2-claim.json",
                encodedNotifyAndSignRecaps
            )
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "Statement I further authorize the stated URI to perform the following actions on my behalf: (1) 'request': 'eth_signTypedData_v4', 'personal_sign' for 'eip155', (2) 'crud': 'notifications', 'subscriptions' for 'https://notify.walletconnect.com/all-apps'\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json\n" +
                "- $encodedNotifyAndSignRecaps"

        assertEquals(message, payload.toCAIP222Message(chainName))
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
            statement = "Statement",
            requestId = null,
            resources = listOf(
                "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
                "https://example.com/my-web2-claim.json",
                encodedSignRecaps
            )
        )

        val message = "service.invalid wants you to sign in with your Ethereum account:\n" +
                "0x15bca56b6e2728aec2532df9d436bd1600e86688\n" +
                "\n" +
                "Statement I further authorize the stated URI to perform the following actions on my behalf: (1) 'request': 'eth_signTypedData_v4', 'personal_sign' for 'eip155'\n" +
                "\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json\n" +
                "- $encodedSignRecaps"

        assertEquals(message, payload.toCAIP222Message(chainName))
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

        assertEquals(message, payload.toCAIP222Message(chainName))
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

        assertEquals(message, payload.toCAIP222Message(chainName))
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

        assertEquals(message, payload.toCAIP222Message(chainName))
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

        assertEquals(message, payload.toCAIP222Message(chainName))
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

        assertEquals(message, payload.toCAIP222Message(chainName))
    }
}