package com.walletconnect.sign.crypto.data.repository

import com.walletconnect.sign.core.model.vo.PrivateKey
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.android_core.crypto.managers.KeyChainMock
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class JwtRepositoryTest {
    private val keyChain = com.walletconnect.android_core.crypto.managers.KeyChainMock()
    private val sut = spyk(com.walletconnect.android_core.crypto.data.repository.JwtRepository(keyChain))
    private val tag = "key_did_keypair"
    private val serverUrl = "wss://relay.walletconnect.com"

    // Expected JWT for given nonce
    private val expectedJWT =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6a2V5Ono2TWtvZEhad25lVlJTaHRhTGY4SktZa3hwREdwMXZHWm5wR21kQnBYOE0yZXh4SCIsInN1YiI6ImM0NzlmZTVkYzQ2NGU3NzFlNzhiMTkzZDIzOWE2NWI1OGQyNzhjYWQxYzM0YmZiMGI1NzE2ZTViYjUxNDkyOGUiLCJhdWQiOiJ3c3M6Ly9yZWxheS53YWxsZXRjb25uZWN0LmNvbSIsImlhdCI6MTY1NjkxMDA5NywiZXhwIjoxNjU2OTk2NDk3fQ.bAKl1swvwqqV_FgwvD4Bx3Yp987B9gTpZctyBviA-EkAuWc8iI8SyokOjkv9GJESgid4U8Tf2foCgrQp2qrxBA"

    @BeforeTest
    fun setUp() {
        val publicKey = PublicKey("884ab67f787b69e534bfdba8d5beb4e719700e90ac06317ed177d49e5a33be5a")
        val privateKey = PrivateKey("58e0254c211b858ef7896b00e3f36beeb13d568d47c6031c4218b87718061295")
        keyChain.setKeys(tag, privateKey, publicKey)

        every { sut.generateSubject() } returns "c479fe5dc464e771e78b193d239a65b58d278cad1c34bfb0b5716e5bb514928e"
        every { sut.encodeByteArray(any()) } answers {
            Base64.getUrlEncoder().withoutPadding().encodeToString(firstArg())
        }
        every { sut.getCurrentTimestamp() } returns 1656910097000L
    }

    @AfterTest
    fun tearDown() {
        keyChain.deleteKeys(tag)
    }

    @Test
    fun generateJWTTest() {
        val actualJWT = sut.generateJWT(serverUrl)
        assertEquals(expectedJWT, actualJWT)
    }
}