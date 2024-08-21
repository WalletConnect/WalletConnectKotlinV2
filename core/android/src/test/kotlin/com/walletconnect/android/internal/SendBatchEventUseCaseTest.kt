package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.model.TelemetryEnabled
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.SendBatchEventUseCase
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.SDKType
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SendBatchEventUseCaseTest {
    private val pulseService: PulseService = mockk()
    private val eventsRepository: EventsRepository = mockk()
    private val telemetryEnabled: TelemetryEnabled = TelemetryEnabled(true)
    private val logger: Logger = mockk()
    private lateinit var useCase: SendBatchEventUseCase
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = SendBatchEventUseCase(pulseService, eventsRepository, telemetryEnabled, logger)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke should send batch events when telemetry is enabled and response is successful`() = runTest(testDispatcher) {
        val props = Props(type = "testEvent")
        val bundleId = "testBundleId"
        val events = listOf(Event(eventId = 1, props = props, bundleId = bundleId), Event(eventId = 2, props = props, bundleId = bundleId))
        coEvery { eventsRepository.getAllEventsWithLimitAndOffset(any(), any()) } returns events andThen listOf()
        coEvery { pulseService.sendEventBatch(any(), any()) } returns Response.success(Unit)
        coEvery { eventsRepository.deleteByIds(any()) } just Runs
        every { logger.log("Sending batch events: ${events.size}") } just Runs

        useCase.invoke()
        advanceUntilIdle()

        coVerify(exactly = 1) { pulseService.sendEventBatch(body = events, sdkType = SDKType.EVENTS.type) }
        coVerify { eventsRepository.deleteByIds(events.map { it.eventId }) }
        verify { logger.log("Sending batch events: ${events.size}") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke should log error and stop processing when response is unsuccessful`() = runTest(testDispatcher) {
        val props = Props(type = "testEvent")
        val bundleId = "testBundleId"
        val events = listOf(Event(eventId = 1, props = props, bundleId = bundleId), Event(eventId = 2, props = props, bundleId = bundleId))
        coEvery { eventsRepository.getAllEventsWithLimitAndOffset(any(), any()) } returns events
        coEvery { pulseService.sendEventBatch(any(), any()) } returns Response.error(400, "Error".toResponseBody())
        every { logger.log("Sending batch events: ${events.size}") } just Runs
        every { logger.log("Failed to send events: ${events.size}") } just Runs

        useCase.invoke()
        advanceUntilIdle()

        coVerify(exactly = 1) { pulseService.sendEventBatch(body = events, sdkType = SDKType.EVENTS.type) }
        verify { logger.log("Failed to send events: ${events.size}") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke should log error and stop processing when exception occurs`() = runTest(testDispatcher) {
        val props = Props(type = "testEvent")
        val bundleId = "testBundleId"
        val events = listOf(Event(eventId = 1, props = props, bundleId = bundleId), Event(eventId = 2, props = props, bundleId = bundleId))
        coEvery { eventsRepository.getAllEventsWithLimitAndOffset(any(), any()) } returns events
        coEvery { pulseService.sendEventBatch(any(), any()) } throws Exception("Test exception")
        every { logger.log("Sending batch events: ${events.size}") } just Runs
        every { logger.error("Error sending batch events: Test exception") } just Runs

        useCase.invoke()
        advanceUntilIdle()

        coVerify(exactly = 1) { pulseService.sendEventBatch(body = events, sdkType = SDKType.EVENTS.type) }
        verify { logger.error("Error sending batch events: Test exception") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke should delete all events when telemetry is disabled`() = runTest(testDispatcher) {
        useCase = SendBatchEventUseCase(pulseService, eventsRepository, TelemetryEnabled(false), logger)
        coEvery { eventsRepository.deleteAllTelemetry() } just Runs

        useCase.invoke()
        advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.deleteAllTelemetry() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke should log error when deleting all events fails`() = runTest(testDispatcher) {
        useCase = SendBatchEventUseCase(pulseService, eventsRepository, TelemetryEnabled(false), logger)
        val exception = Exception("Test exception")
        coEvery { eventsRepository.deleteAllTelemetry() } throws exception
        every { logger.error("Failed to delete events, error: java.lang.Exception: Test exception") } just Runs

        useCase.invoke()
        advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.deleteAllTelemetry() }
        verify { logger.error("Failed to delete events, error: $exception") }
    }
}