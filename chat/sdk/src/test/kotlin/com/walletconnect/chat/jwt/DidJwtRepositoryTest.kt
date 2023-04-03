package com.walletconnect.chat.jwt

import android.util.Log
import com.walletconnect.android.internal.common.jwt.extractVerifiedDidJwtClaims
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test

internal class DidJwtRepositoryTest {

    init {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun jwtDecode() {
        org.junit.jupiter.api.assertDoesNotThrow {
            val jwt = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9" +
                    ".eyJpc3MiOiJkaWQ6a2V5Ono2TWt1VHhCUTR5aTN1Y3pUWlJUYkxKVFNLaGUzN2phUnJzdEJpUWg2SmIxZ3QzWCIsImlhdCI6MTY3NTQzMjI2NiwiZXhwIjoxNjc1NTE4NjY2LCJrc3UiOiJodHRwczovL3N0YWdpbmcua2V5cy53YWxsZXRjb25uZWN0LmNvbSIsImF1ZCI6ImRpZDpwa2g6ZWlwMTU1OjE6MHhlN2M3M2JhNjlhZGM2NTkzZDM3YmQ2ZmUwMDk5NWZiODljNTQxMjg2Iiwic3ViIjoiSGV5ISIsInBrZSI6ImRpZDprZXk6ejZMU21CQ0JLd0NaemJDd3J2RWFpdFU5TFJNMnRWOWYzWHVVN3FzTUJ4bkN6U1RrIn0.NdalJAlosJygMG4gsh0ZgKtVLvEm473E4cBRmqkvZX_M1KahHuyk59ZEB9VMnqvXjnmbfMEcMvKt-unRhNsDDw"
            extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteProposal>(jwt).getOrThrow()
        }
    }
}