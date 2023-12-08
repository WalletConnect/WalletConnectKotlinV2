package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RespondSessionRequestUseCaseTest {
    private val jsonRpcInteractor = mockk<JsonRpcInteractorInterface>()
    private val sessionStorageRepository = mockk<SessionStorageRepository>()
    private val getPendingJsonRpcHistoryEntryByIdUseCase = mockk<GetPendingJsonRpcHistoryEntryByIdUseCase>()
    private val logger = mockk<Logger>()
    private val verifyContextStorageRepository = mockk<VerifyContextStorageRepository>()
    private val respondSessionRequestUseCase = RespondSessionRequestUseCase(
        jsonRpcInteractor,
        sessionStorageRepository,
        getPendingJsonRpcHistoryEntryByIdUseCase,
        logger,
        verifyContextStorageRepository
    )

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @Test
    fun `onFailure is called when sessionStorageRepository isSessionValid is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns false

        respondSessionRequestUseCase.respondSessionRequest(
            topic = "topic",
            jsonRpcResponse = mockk(),
            onSuccess = {
                Assert.fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                Assert.assertSame(CannotFindSequenceForTopic::class, error::class)
            }
        )
    }
}