package com.walletconnect.android.internal

import com.walletconnect.android.verify.domain.JWTRepository
import com.walletconnect.android.verify.model.JWK
import com.walletconnect.util.hexToBytes
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class JWTRepositoryTest {

    private val sut = spyk(JWTRepository())

    @Test
    fun `create a key and verify JWT`() {
        val jwk = JWK(
            kty = "EC",
            crv = "P-256",
            x = "CbL4DOYOb1ntd-8OmExO-oS0DWCMC00DntrymJoB8tk",
            y = "KTFwjHtQxGTDR91VsOypcdBfvbo6sAMj5p4Wb-9hRA0",
            keyOps = listOf("verify"),
            ext = true
        )
        val jwt =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MjI1Nzk5MDgsImlkIjoiNTEwNmEyNTU1MmU4OWFjZmI1YmVkODNlZTIxYmY0ZTgwZGJjZDUxYjBiMjAzZjY5MjVhMzY5YWFjYjFjODYwYiIsIm9yaWdpbiI6Imh0dHBzOi8vcmVhY3QtZGFwcC12Mi1naXQtY2hvcmUtdmVyaWZ5LXYyLXNhbXBsZXMtd2FsbGV0Y29ubmVjdDEudmVyY2VsLmFwcCIsImlzU2NhbSI6bnVsbCwiaXNWZXJpZmllZCI6dHJ1ZX0.vm1TUofxpKc6yLYXDgR_p7AYhTC9_WMu9FOgY7l3fMAX_COgqIBGaY9NE8Sq8WmDGjTJroF15qsy9xD8dUXIcw"
        val publicKey = sut.generateP256PublicKeyFromJWK(jwk)

        val isValid = sut.verifyJWT(jwt, publicKey.hexToBytes())
        assert(isValid)
    }

    private val jwtRepository = JWTRepository()

    @Test
    fun `generateP256PublicKeyFromJWK should generate valid public key`() {
        val jwk = mockk<JWK>()
        every { jwk.x } returns "CbL4DOYOb1ntd-8OmExO-oS0DWCMC00DntrymJoB8tk"
        every { jwk.y } returns "KTFwjHtQxGTDR91VsOypcdBfvbo6sAMj5p4Wb-9hRA0"

        val xBytes = byteArrayOf(1, 2, 3) // Replace with actual bytes
        val yBytes = byteArrayOf(4, 5, 6) // Replace with actual bytes
        every { io.ipfs.multibase.binary.Base64.decodeBase64("base64encodedX") } returns xBytes
        every { io.ipfs.multibase.binary.Base64.decodeBase64("base64encodedY") } returns yBytes

        val publicKey = jwtRepository.generateP256PublicKeyFromJWK(jwk)
        assertNotNull(publicKey)
    }

    @Test
    fun `verifyJWT should return true for valid JWT`() {
        val jwt = "header.claims.signature"
        val publicKey = byteArrayOf(1, 2, 3) // Replace with actual bytes

        // Mock Base64 decoding
        val signature = byteArrayOf(7, 8, 9, 10) // Replace with actual signature bytes
        every { io.ipfs.multibase.binary.Base64.decodeBase64("signature") } returns signature

        val result = jwtRepository.verifyJWT(jwt, publicKey)
        assertTrue(result)
    }

    @Test
    fun `verifyJWT should throw error for invalid JWT`() {
        val invalidJwt = "invalid.jwt"

        val isValid = jwtRepository.verifyJWT(invalidJwt, byteArrayOf(1, 2, 3))
        assertEquals(false, isValid)
    }

    @Test
    fun `decodeClaimsJWT should return decoded claims for valid JWT`() {
        val jwt = "header.claims.signature"

        // Mock Base64 decoding
        val claimsBytes = "claimsPayload".toByteArray()
        every { io.ipfs.multibase.binary.Base64.decodeBase64("claims") } returns claimsBytes

        val claims = jwtRepository.decodeClaimsJWT(jwt)
        assertEquals("claimsPayload", claims)
    }

    @Test
    fun `decodeClaimsJWT should throw error for invalid JWT`() {
        val invalidJwt = "invalid.jwt"

        val exception = assertThrows(Throwable::class.java) {
            jwtRepository.decodeClaimsJWT(invalidJwt)
        }
        assertEquals("Unable to split jwt: $invalidJwt", exception.message)
    }
}