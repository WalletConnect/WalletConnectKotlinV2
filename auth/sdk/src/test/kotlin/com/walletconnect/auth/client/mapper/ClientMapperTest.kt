package com.walletconnect.auth.client.mapper

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ISO_8601_PATTERN
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

internal class ClientMapperTest {
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val dummyPairing = Core.Model.Pairing("", 0L, null, "", null, "", true, "")

    private fun Cacao.Payload.mockIatAsNbf(request: Auth.Params.Request): Cacao.Payload {
        return this.copy(iat = request.nbf!!)
    }

    private fun Auth.Params.Request.toCacaoPayload(iss: String): Cacao.Payload = this.toCommon().toCacaoPayload(Issuer(iss))

    @Test
    fun `Payload based on Request mapping with supplied issuer`() {
        val request = Auth.Params.Request(
            topic = dummyPairing.topic,
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

        val payload = Cacao.Payload(
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
        val before = Instant.now(Clock.offset(Clock.systemDefaultZone(), Duration.ofSeconds(-2)))

        val payload = Auth.Params.Request(
            topic = dummyPairing.topic,
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

        val sd = SimpleDateFormat(ISO_8601_PATTERN).parse(payload.iat) ?: fail("Cannot parse iat")
        val iat = Instant.ofEpochMilli(sd.time)
        val isAfter = Instant.now().isAfter(iat)
        val isBefore = before.isBefore(iat)
        assertTrue(isAfter)
        assertTrue(isBefore)
    }
}