package com.walletconnect.android.internal

import com.walletconnect.android.verify.domain.JWTRepository
import com.walletconnect.android.verify.model.JWK
import com.walletconnect.util.hexToBytes
import io.mockk.spyk
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
}