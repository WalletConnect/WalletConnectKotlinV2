package com.walletconnect.sign.crypto.data.repository

import com.walletconnect.sign.core.model.vo.PrivateKey
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.crypto.managers.KeyChainMock
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class JwtRepositoryTest {
    private val keyChain = KeyChainMock()
    private val sut = spyk(JwtRepository(keyChain))
    private val tag = "key_did_keypair"

    // Expected JWT for given nonce
    private val expectedJWT =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6a2V5Ono2TWtvZEhad25lVlJTaHRhTGY4SktZa3hwREdwMXZHWm5wR21kQnBYOE0yZXh4SCIsInN1YiI6ImM0NzlmZTVkYzQ2NGU3NzFlNzhiMTkzZDIzOWE2NWI1OGQyNzhjYWQxYzM0YmZiMGI1NzE2ZTViYjUxNDkyOGUifQ.0JkxOM-FV21U7Hk-xycargj_qNRaYV2H5HYtE4GzAeVQYiKWj7YySY5AdSqtCgGzX4Gt98XWXn2kSr9rE1qvCA"

    @BeforeTest
    fun setUp() {
        val publicKey = PublicKey("884ab67f787b69e534bfdba8d5beb4e719700e90ac06317ed177d49e5a33be5a")
        val privateKey = PrivateKey("58e0254c211b858ef7896b00e3f36beeb13d568d47c6031c4218b87718061295")
        keyChain.setKeys(tag, privateKey, publicKey)

        every { sut.generateSubject() } returns "c479fe5dc464e771e78b193d239a65b58d278cad1c34bfb0b5716e5bb514928e"
        every { sut.encodeByteArray(any()) } answers  {
            Base64.getUrlEncoder().withoutPadding().encodeToString(firstArg())
        }
    }

    @AfterTest
    fun tearDown() {
        keyChain.deleteKeys(tag)
    }

    @Test
    fun generateJWTTest() {
        val actualJWT = sut.generateJWT()
        assertEquals(expectedJWT, actualJWT )
    }
}