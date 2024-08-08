package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

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
        val event = EngineDO.Event("accountsChanged", "data", "eip155:1")
        val session = SessionVO(
            topic = Topic("topic"),
            expiry = Expiry(0),
            relayProtocol = "relayProtocol",
            relayData = "relayData",
            controllerKey = PublicKey("key"),
            selfPublicKey = PublicKey("key"),
            sessionNamespaces = mapOf(
                "eip155" to Namespace.Session(
                    chains = listOf("eip155:1"),
                    methods = listOf("eth_sign", "eth_sendTransaction"),
                    events = listOf("accountsChanged"),
                    accounts = listOf("0x1234567890123456789012345678901234567890")
                )
            ),
            requiredNamespaces = emptyMap(),
            optionalNamespaces = emptyMap(),
            isAcknowledged = false,
            pairingTopic = "pairingTopic",
            transportType = null
        )

        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        every { sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic)) } returns session
        every { logger.log(any<String>()) } just Runs

        emitEventUseCase.emit(topic, event, 1L, onSuccess = onSuccess, onFailure = onFailure)

        verify { logger.log("Emitting event on topic: $topic") }
    }
}