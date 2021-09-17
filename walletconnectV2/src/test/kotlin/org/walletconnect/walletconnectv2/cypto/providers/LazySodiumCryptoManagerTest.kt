package org.walletconnect.walletconnectv2.cypto.providers

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LazySodiumCryptoManagerTest {
    private val sut = LazySodiumCryptoManager()

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `Verify that the generated public key is a valid key`() {
        val publicKey = sut.generateKeyPair()

        assert(publicKey.key.length == 64)
    }

}