package com.walletconnect.foundation

import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.network.model.RelayDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi

class BaseRelayClientTest {
    private lateinit var client: BaseRelayClient
    private val relayServiceMock = mockk<RelayService>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        client = object : BaseRelayClient() {
            init {
                this.relayService = relayServiceMock
            }
        }
    }

    @Test
    fun `test subscribe success`() = testScope.runTest {
        // Prepare
        val topic = "testTopic"
        val expectedId = 123L
        val relayDto = RelayDTO.Subscribe.Result.Acknowledgement(id = expectedId, result = SubscriptionId("testId"))

        coEvery { relayServiceMock.subscribeRequest(any()) } returns Unit
        coEvery { relayServiceMock.observeSubscribeAcknowledgement() } returns flowOf(relayDto)

        // Execute
        client.subscribe(topic) { result ->
            assertTrue(result.isSuccess)
            assertEquals(expectedId, result.getOrNull()?.id)
        }

        // Verify
        coVerify(exactly = 1) { relayServiceMock.subscribeRequest(any()) }
    }

    @Test
    fun `test subscribe failure due to timeout`() = testScope.runTest {
        // Prepare
        val topic = "testTopic"
        coEvery { relayServiceMock.subscribeRequest(any()) } returns Unit
        coEvery { relayServiceMock.observeSubscribeAcknowledgement() } returns flow { delay(10000L) }

        // Execute
        client.subscribe(topic) { result ->
            assertTrue(result.isFailure)
            assertEquals("Subscribe timed out", result.exceptionOrNull()?.message)
        }

        // Advance time to trigger timeout
        testScheduler.apply { advanceTimeBy(5000); runCurrent() }

        // Verify
        coVerify(exactly = 1) { relayServiceMock.subscribeRequest(any()) }
    }
}