package com.walletconnect.android.internal

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.TelemetryEnabled
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.sdk.storage.data.dao.EventQueries
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class EventsRepositoryTest {
    private val eventQueries: EventQueries = mockk()
    private val telemetryEnabled: TelemetryEnabled = TelemetryEnabled(true)
    private val bundleId: String = "testBundleId"
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: EventsRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = EventsRepository(eventQueries, bundleId, telemetryEnabled, testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertOrAbort should insert event when telemetry is enabled`() = runTest(testDispatcher) {
        val props = Props(event = "testEvent", type = "testType")
        every { eventQueries.insertOrAbort(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } just Runs

        repository.insertOrAbortTelemetry(props)

        verify {
            eventQueries.insertOrAbort(
                event_id = any(),
                bundle_id = bundleId,
                timestamp = any(),
                event_name = "testEvent",
                type = "testType",
                topic = null,
                trace = null,
                correlation_id = any(),
                client_id = any(),
                direction = any(),
                user_agent = any()
            )
        }
    }

    @Test
    fun `insertOrAbort should insert event `() = runTest(testDispatcher) {
        val props = Props(event = "testEvent", type = "testType")
        every { eventQueries.insertOrAbort(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } just Runs

        repository.insertOrAbort(props)

        verify {
            eventQueries.insertOrAbort(
                event_id = any(),
                bundle_id = bundleId,
                timestamp = any(),
                event_name = "testEvent",
                type = "testType",
                topic = null,
                trace = null,
                correlation_id = any(),
                client_id = any(),
                direction = any(),
                user_agent = any()
            )
        }
    }

    @Test
    fun `insertOrAbort should not insert event when telemetry is disabled`() = runTest(testDispatcher) {
        repository = EventsRepository(eventQueries, bundleId, TelemetryEnabled(false), testDispatcher)
        val props = Props(event = "testEvent", type = "testType")

        repository.insertOrAbortTelemetry(props)

        verify(exactly = 0) {
            eventQueries.insertOrAbort(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `insertOrAbort should throw SQLiteException when insertion fails`() = runTest(testDispatcher) {
        val props = Props(event = "testEvent", type = "testType")
        every { eventQueries.insertOrAbort(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws SQLiteException()

        assertFailsWith<SQLiteException> {
            repository.insertOrAbortTelemetry(props)
        }
    }

    @Test
    fun `deleteAll should delete all events`() = runTest(testDispatcher) {
        coEvery { eventQueries.deleteAllTelemetry() } just Runs

        repository.deleteAllTelemetry()

        coVerify { eventQueries.deleteAllTelemetry() }
    }

    @Test
    fun `deleteByIds should delete events by ids`() = runTest(testDispatcher) {
        val eventIds = listOf(1L, 2L, 3L)
        coEvery { eventQueries.deleteByIds(eventIds) } just Runs

        repository.deleteByIds(eventIds)

        coVerify { eventQueries.deleteByIds(eventIds) }
    }
}