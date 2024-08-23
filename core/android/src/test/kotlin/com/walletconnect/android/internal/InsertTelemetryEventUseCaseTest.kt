package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.domain.InsertTelemetryEventUseCase
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class InsertTelemetryEventUseCaseTest {

    private val eventsRepository: EventsRepository = mockk()
    private val logger: Logger = mockk()
    private val useCase = InsertTelemetryEventUseCase(eventsRepository, logger)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke should call insertOrAbort on eventsRepository with given props`() = runTest(testDispatcher) {
        val props = Props(type = "test")

        coEvery { eventsRepository.insertOrAbortTelemetry(props) } just Runs

        useCase(props)

        coVerify { eventsRepository.insertOrAbortTelemetry(props) }
        confirmVerified(eventsRepository)
    }
    @Test
    fun `invoke should log error when eventsRepository throws exception`() = runTest(testDispatcher) {
        val props = Props(type = "test")
        val exception = Exception("Test exception")

        coEvery { eventsRepository.insertOrAbortTelemetry(props) } throws exception
        every { logger.error("Inserting event test error: java.lang.Exception: Test exception") } just Runs

        useCase(props)

        verify { logger.error("Inserting event test error: java.lang.Exception: Test exception") }
        confirmVerified(logger)
    }
}