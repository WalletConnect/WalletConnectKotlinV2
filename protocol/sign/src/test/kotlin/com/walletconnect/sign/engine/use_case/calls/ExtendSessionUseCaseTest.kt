package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.NotSettledSessionException
import com.walletconnect.sign.common.exceptions.UnauthorizedPeerException
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ExtendSessionUseCaseTest {
    private val jsonRpcInteractor = mockk<JsonRpcInteractorInterface>()
    private val sessionStorageRepository = mockk<SessionStorageRepository>()
    private val logger = mockk<Logger>()
    private val extendSessionUseCase = ExtendSessionUseCase(jsonRpcInteractor, sessionStorageRepository, logger)

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @Test
    fun `onFailure is called when sessionStorageRepository isSessionValid is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns false

        extendSessionUseCase.extend(
            topic = "topic",
            onSuccess = {
                fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                assertSame(CannotFindSequenceForTopic::class, error::class)
            }
        )
    }

    @Test
    fun `onFailure is called when session isSelfController is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns true
        every { sessionStorageRepository.getSessionWithoutMetadataByTopic(any()) } returns SessionVO(
            topic = Topic("topic"),
            expiry = Expiry(0),
            relayProtocol = "relayProtocol",
            relayData = "relayData",
            selfPublicKey = PublicKey("selfPublicKey"),
            sessionNamespaces = emptyMap(),
            requiredNamespaces = emptyMap(),
            optionalNamespaces = emptyMap(),
            isAcknowledged = false,
            pairingTopic = "pairingTopic"
        )

        extendSessionUseCase.extend(
            topic = "topic",
            onSuccess = {
                fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                assertSame(UnauthorizedPeerException::class, error::class)
            }
        )
    }

    @Test
    fun `onFailure is called when session isAcknowledged is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns true
        every { sessionStorageRepository.getSessionWithoutMetadataByTopic(any()) } returns SessionVO(
            topic = Topic("topic"),
            expiry = Expiry(0),
            relayProtocol = "relayProtocol",
            relayData = "relayData",
            controllerKey = PublicKey(""),
            selfPublicKey = PublicKey(""),
            sessionNamespaces = emptyMap(),
            requiredNamespaces = emptyMap(),
            optionalNamespaces = emptyMap(),
            isAcknowledged = false,
            pairingTopic = "pairingTopic"
        )

        extendSessionUseCase.extend(
            topic = "topic",
            onSuccess = {
                fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                assertSame(NotSettledSessionException::class, error::class)
            }
        )
    }
}