package com.walletconnect.android.internal.common

import com.walletconnect.android.internal.KeyChainMock
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.jwtIatAndExp
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ClientIdJwtRepositoryAndroidTest {

    private val keyChain = KeyChainMock()
    private val sut = spyk(ClientIdJwtRepositoryAndroid(keyChain))
    private val tag = "key_did_keypair"
    private val serverUrl = "wss://relay.walletconnect.com"

    // Expected JWT for given nonce
    private val expectedJWT =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6a2V5Ono2TWtvZEhad25lVlJTaHRhTGY4SktZa3hwREdwMXZHWm5wR21kQnBYOE0yZXh4SCIsInN1YiI6ImM0NzlmZTVkYzQ2NGU3NzFlNzhiMTkzZDIzOWE2NWI1OGQyNzhjYWQxYzM0YmZiMGI1NzE2ZTViYjUxNDkyOGUiLCJhdWQiOiJ3c3M6Ly9yZWxheS53YWxsZXRjb25uZWN0LmNvbSIsImlhdCI6MTY1NjkxMDA5NywiZXhwIjoxNjU2OTk2NDk3fQ.bAKl1swvwqqV_FgwvD4Bx3Yp987B9gTpZctyBviA-EkAuWc8iI8SyokOjkv9GJESgid4U8Tf2foCgrQp2qrxBA"

    @BeforeEach
    fun setUp() {
        val publicKey = PublicKey("884ab67f787b69e534bfdba8d5beb4e719700e90ac06317ed177d49e5a33be5a")
        val privateKey = PrivateKey("58e0254c211b858ef7896b00e3f36beeb13d568d47c6031c4218b87718061295")
        keyChain.setKeys(tag, privateKey, publicKey)

        every { sut.generateSubject() } returns "c479fe5dc464e771e78b193d239a65b58d278cad1c34bfb0b5716e5bb514928e"
        mockkStatic(::jwtIatAndExp)
        every { jwtIatAndExp(any(), any(), any(), any()) } returns (1656910097L to (1656910097L + 86400L))
    }

    @AfterEach
    fun tearDown() {
        keyChain.deleteKeys(tag)
    }

    @Test
    fun generateJWTTest() {
        val actualJWT = sut.generateJWT(serverUrl)
        assertEquals(expectedJWT, actualJWT)
    }
}