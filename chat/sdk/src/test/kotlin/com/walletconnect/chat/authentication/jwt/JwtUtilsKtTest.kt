package com.walletconnect.chat.authentication.jwt

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class JwtUtilsKtTest {
    private val didJwtRepository = DidJwtRepository()

    init {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun jwtDecode() {
        assertDoesNotThrow {
            val jwt = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9" +
                    ".eyJpc3MiOiJkaWQ6a2V5Ono2TWt1VHhCUTR5aTN1Y3pUWlJUYkxKVFNLaGUzN2phUnJzdEJpUWg2SmIxZ3QzWCIsImlhdCI6MTY3NTQzMjI2NiwiZXhwIjoxNjc1NTE4NjY2LCJrc3UiOiJodHRwczovL3N0YWdpbmcua2V5cy53YWxsZXRjb25uZWN0LmNvbSIsImF1ZCI6ImRpZDpwa2g6ZWlwMTU1OjE6MHhlN2M3M2JhNjlhZGM2NTkzZDM3YmQ2ZmUwMDk5NWZiODljNTQxMjg2Iiwic3ViIjoiSGV5ISIsInBrZSI6ImRpZDprZXk6ejZMU21CQ0JLd0NaemJDd3J2RWFpdFU5TFJNMnRWOWYzWHVVN3FzTUJ4bkN6U1RrIn0.NdalJAlosJygMG4gsh0ZgKtVLvEm473E4cBRmqkvZX_M1KahHuyk59ZEB9VMnqvXjnmbfMEcMvKt-unRhNsDDw"
            didJwtRepository.extractVerifiedDidJwtClaims<JwtClaims.InviteProposal>(jwt).getOrThrow()
        }
    }
}