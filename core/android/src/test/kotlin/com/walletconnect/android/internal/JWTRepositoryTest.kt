package com.walletconnect.android.internal

import com.walletconnect.android.verify.domain.JWTRepository
import com.walletconnect.android.verify.model.JWK
import com.walletconnect.util.hexToBytes
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class JWTRepositoryTest {
    private val sut = spyk(JWTRepository())

    @Test
    fun `verifyJWT should return true for valid JWT`() {
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

    @Test
    fun `generateP256PublicKeyFromJWK should generate valid public key`() {
        val jwk = JWK(
            kty = "EC",
            crv = "P-256",
            x = "CbL4DOYOb1ntd-8OmExO-oS0DWCMC00DntrymJoB8tk",
            y = "KTFwjHtQxGTDR91VsOypcdBfvbo6sAMj5p4Wb-9hRA0",
            keyOps = listOf("verify"),
            ext = true
        )

        val publicKey = sut.generateP256PublicKeyFromJWK(jwk)
        assertNotNull(publicKey)
    }

    @Test
    fun `verifyJWT should throw error for invalid JWT`() {
        val invalidJwt = "invalid.jwt.sig"
        val jwk = JWK(
            kty = "EC",
            crv = "P-256",
            x = "CbL4DOYOb1ntd-8OmExO-oS0DWCMC00DntrymJoB8tk",
            y = "KTFwjHtQxGTDR91VsOypcdBfvbo6sAMj5p4Wb-9hRA0",
            keyOps = listOf("verify"),
            ext = true
        )

        val publicKey = sut.generateP256PublicKeyFromJWK(jwk)

        val isValid = sut.verifyJWT(invalidJwt, publicKey.hexToBytes())
        assertEquals(false, isValid)
    }

    @Test
    fun `decodeClaimsJWT should return decoded claims for valid JWT`() {
        val jwt =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MjI1Nzk5MDgsImlkIjoiNTEwNmEyNTU1MmU4OWFjZmI1YmVkODNlZTIxYmY0ZTgwZGJjZDUxYjBiMjAzZjY5MjVhMzY5YWFjYjFjODYwYiIsIm9yaWdpbiI6Imh0dHBzOi8vcmVhY3QtZGFwcC12Mi1naXQtY2hvcmUtdmVyaWZ5LXYyLXNhbXBsZXMtd2FsbGV0Y29ubmVjdDEudmVyY2VsLmFwcCIsImlzU2NhbSI6bnVsbCwiaXNWZXJpZmllZCI6dHJ1ZX0.vm1TUofxpKc6yLYXDgR_p7AYhTC9_WMu9FOgY7l3fMAX_COgqIBGaY9NE8Sq8WmDGjTJroF15qsy9xD8dUXIcw"
        val claims = sut.decodeClaimsJWT(jwt)
        assert(claims.contains("isScam"))
        assert(claims.contains("isVerified"))
    }

    @Test
    fun `decodeClaimsJWT should throw error for invalid JWT`() {
        val invalidJwt = "invalid.jwt"

        val exception = assertThrows(Throwable::class.java) {
            sut.decodeClaimsJWT(invalidJwt)
        }
        assertEquals("Unable to split jwt: $invalidJwt", exception.message)
    }
}