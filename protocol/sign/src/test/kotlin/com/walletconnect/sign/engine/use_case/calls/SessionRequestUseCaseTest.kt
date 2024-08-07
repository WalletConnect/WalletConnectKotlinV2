package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SessionRequestUseCaseTest {
    private val sessionStorageRepository = mockk<SessionStorageRepository>()
    private val jsonRpcInteractor = mockk<RelayJsonRpcInteractorInterface>()
    private val logger = mockk<Logger>()
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface = mockk()
    private val metadataStorageRepository = mockk<MetadataStorageRepositoryInterface>()
    private val insertEventUseCase = mockk<InsertEventUseCase>()
    private val sessionRequestUseCase = SessionRequestUseCase(
        sessionStorageRepository,
        jsonRpcInteractor,
        linkModeJsonRpcInteractor,
        metadataStorageRepository,
        insertEventUseCase,
        "clientId",
        logger
    )

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @Test
    fun `onFailure is called when sessionStorageRepository isSessionValid is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns false

        sessionRequestUseCase.sessionRequest(
            request = EngineDO.Request(
                topic = "topic",
                method = "method",
                params = "params",
                chainId = "chainId",
            ),
            onSuccess = {
                fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                assertSame(CannotFindSequenceForTopic::class, error::class)
            }
        )
    }
}