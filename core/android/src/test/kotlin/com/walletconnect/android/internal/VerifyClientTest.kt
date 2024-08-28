package com.walletconnect.android.internal

import com.walletconnect.android.verify.client.VerifyClient
import com.walletconnect.android.verify.domain.VerifyRepository
import com.walletconnect.android.verify.domain.VerifyResult
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class VerifyClientTest {
    private lateinit var koinApp: KoinApplication
    private var verifyRepository: VerifyRepository = mockk(relaxed = true)
    private lateinit var verifyClient: VerifyClient
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        koinApp = startKoin {
            modules(module {
                single { verifyRepository }
            })
        }
        verifyClient = VerifyClient(koinApp, testScope)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `resolve should call resolve on verifyRepository`() = runTest {
        val attestationId = "attestationId"
        val metadataUrl = "https://metadata.url"
        val onSuccess = mockk<(VerifyResult) -> Unit>(relaxed = true)
        val onError = mockk<(Throwable) -> Unit>(relaxed = true)

        verifyClient.resolve(attestationId, metadataUrl, onSuccess, onError)

        coVerify { verifyRepository.resolve(attestationId, metadataUrl, onSuccess, onError) }
    }

    @Test
    fun `resolveV2 should call resolveV2 on verifyRepository`() = runTest {
        val attestation = "attestation"
        val metadataUrl = "https://metadata.url"
        val onSuccess = mockk<(VerifyResult) -> Unit>(relaxed = true)
        val onError = mockk<(Throwable) -> Unit>(relaxed = true)

        verifyClient.resolveV2("id", attestation, metadataUrl, onSuccess, onError)

        coVerify { verifyRepository.resolveV2("id", attestation, metadataUrl, onSuccess, onError) }
    }
}