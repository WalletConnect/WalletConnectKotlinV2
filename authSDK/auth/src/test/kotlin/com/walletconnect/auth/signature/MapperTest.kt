package com.walletconnect.auth.signature

import com.walletconnect.auth.client.Auth.Params.Request
import com.walletconnect.auth.client.mapper.toEngineDO
import com.walletconnect.auth.common.model.IssuerVO
import com.walletconnect.auth.common.model.CacaoVO.PayloadVO as Payload
import com.walletconnect.auth.engine.model.mapper.toCacaoPayloadDTO
import com.walletconnect.auth.engine.model.mapper.toDTO
import com.walletconnect.auth.engine.model.mapper.toVO
import com.walletconnect.auth.engine.model.mapper.toFormattedMessage
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Clock.systemDefaultZone
import java.time.Duration
import java.time.ZonedDateTime
import java.time.chrono.ChronoZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MapperTest {
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val chainName = "Ethereum"

    private fun Payload.mockIatAsNbf(request: Request): Payload {
        return this.copy(iat = request.nbf!!)
    }
    private fun Request.toCacaoPayload(iss: String): Payload = this.toEngineDO().toDTO().toCacaoPayloadDTO(IssuerVO(iss)).toVO()

    @Test
    fun `Payload based on Request mapping with supplied issuer`() {
        val request = Request(
            type = "eip191",
            chainId = "eip155:1",
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            nonce = "32891756",
            nbf = "2021-09-30T16:25:24Z",
            exp = null,
            statement = "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
            requestId = null,
            resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/", "https://example.com/my-web2-claim.json")
        )

        val payload = Payload(
            iss = iss,
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            version = "1",
            nonce = "32891756",
            iat = "2021-09-30T16:25:24Z",
            nbf = "2021-09-30T16:25:24Z",
            exp = null,
            statement = "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
            requestId = null,
            resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/", "https://example.com/my-web2-claim.json")
        )

        assertEquals(payload, request.toCacaoPayload(iss).mockIatAsNbf(request))
    }

    @Test
    fun `Payload based on Request generates issued at with current time`() {
        val before = ZonedDateTime.now(Clock.offset(systemDefaultZone(), Duration.ofSeconds(-2)))

        val payload = Request(
            type = "eip191",
            chainId = "eip155:1",
            domain = "service.invalid",
            aud = "https://service.invalid/login",
            nonce = "32891756",
            nbf = "2021-09-30T16:25:24Z",
            exp = null,
            statement = "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
            requestId = null,
            resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/", "https://example.com/my-web2-claim.json")
        ).toCacaoPayload(iss)

        val iat = ChronoZonedDateTime.from(DateTimeFormatter.ofPattern(ISO_8601_PATTERN).parse(payload.iat))
        val isAfter = ZonedDateTime.now().isAfter(iat)
        val isBefore = before.isBefore(iat)
        assertTrue(isBefore)
        assertTrue(isAfter)
    }

    @Test
    fun `Payload required fields formatting`() {
        val payload = Payload(
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
                "\n\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z"

        assertEquals(message, payload.toFormattedMessage(chainName))
    }

    @Test
    fun `Payload resources formatting`() {
        val payload = Payload(
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
                "\n\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Resources:\n" +
                "- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/\n" +
                "- https://example.com/my-web2-claim.json"

        assertEquals(message, payload.toFormattedMessage(chainName))
    }

    @Test
    fun `Payload requestId formatting`() {
        val payload = Payload(
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
                "\n\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Request ID: someRequestId"

        assertEquals(message, payload.toFormattedMessage(chainName))
    }

    @Test
    fun `Payload statement formatting`() {
        val payload = Payload(
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

        assertEquals(message, payload.toFormattedMessage(chainName))
    }

    @Test
    fun `Payload expiry formatting`() {
        val payload = Payload(
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
                "\n\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Expiration Time: 2021-09-31T16:25:24Z"

        assertEquals(message, payload.toFormattedMessage(chainName))
    }

    @Test
    fun `Payload not before formatting`() {
        val payload = Payload(
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
                "\n\n" +
                "URI: https://service.invalid/login\n" +
                "Version: 1\n" +
                "Chain ID: 1\n" +
                "Nonce: 32891756\n" +
                "Issued At: 2021-09-30T16:25:24Z\n" +
                "Not Before: 2021-09-31T16:25:24Z"

        assertEquals(message, payload.toFormattedMessage(chainName))
    }

    @Test
    fun `Payload all fields formatting`() {
        val payload = Payload(
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

        assertEquals(message, payload.toFormattedMessage(chainName))
    }
}