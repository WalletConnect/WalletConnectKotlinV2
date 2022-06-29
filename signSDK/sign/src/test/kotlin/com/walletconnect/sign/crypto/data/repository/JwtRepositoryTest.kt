package com.walletconnect.sign.crypto.data.repository

import android.content.SharedPreferences
import com.walletconnect.sign.crypto.managers.KeyChainMock
import com.walletconnect.sign.network.data.service.NonceService
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class JwtRepositoryTest {
    private val sharedPreferences = mockk<SharedPreferences>()
    private val nonceService = mockk<NonceService>()
    private val sut = JwtRepository(sharedPreferences, KeyChainMock(), nonceService)

    @Test
    fun jwtExists() {
    }

    @Test
    fun getJWT() {
    }

    @Test
    fun signJWT() {
    }

    @Test
    fun getNonceFromNewDID() {
    }
}