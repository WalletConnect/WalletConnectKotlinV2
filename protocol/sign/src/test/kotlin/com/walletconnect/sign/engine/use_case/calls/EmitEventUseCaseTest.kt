package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.fail

class EmitEventUseCaseTest {
    private val jsonRpcInteractor = mockk<RelayJsonRpcInteractorInterface>(relaxed = true)
    private val sessionStorageRepository = mockk<SessionStorageRepository>(relaxed = true)
    private val logger = mockk<Logger>(relaxed = true)
    private val emitEventUseCase = EmitEventUseCase(jsonRpcInteractor, sessionStorageRepository, logger)
    private val onSuccess: () -> Unit = mockk(relaxed = true)
    private val onFailure: (Throwable) -> Unit = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @Test
    fun `onFailure is called when sessionStorageRepository isSessionValid is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns false

        emitEventUseCase.emit(
            topic = "topic",
            event = EngineDO.Event("name", "data", "chainId"),
            onSuccess = {
                Assert.fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                Assert.assertSame(CannotFindSequenceForTopic::class, error::class)
            }
        )
    }

    @Test
    fun `emit should successfully emit event`() = runTest {
        val topic = "test_topic"
        val event = mockk<EngineDO.Event>(relaxed = true) {
            every { name } returns "accountsChanged"
            every { data } returns "event_data"
            every { chainId } returns "eip155:1"
        }
        val session = mockk<SessionVO>(relaxed = true) {
            every { isSelfController } returns true
            every { sessionNamespaces } returns mapOf(
                "eip155" to Namespace.Session(
                    chains = listOf("eip155:1"),
                    methods = listOf("eth_sign", "eth_sendTransaction"),
                    events = listOf("accountsChanged"),
                    accounts = listOf("0x1234567890123456789012345678901234567890")
                )
            )
        }

        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        every { sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic)) } returns session

        emitEventUseCase.emit(topic, event, null, onSuccess = {
            assertTrue(true)
        }, onFailure = {
            fail("Emit failed: $it")
        })

        verify { logger.log("Emitting event on topic: $topic") }
        verify {
            jsonRpcInteractor.publishJsonRpcRequest(
                eq(Topic(topic)),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }
}