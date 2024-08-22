package com.walletconnect.android.internal
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsertEventUseCaseTest {
    private val eventsRepository: EventsRepository = mockk(relaxed = true)
    private val logger: Logger = mockk(relaxed = true)

    private lateinit var insertEventUseCase: InsertEventUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        insertEventUseCase = InsertEventUseCase(eventsRepository, logger)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke should call insertOrAbort on eventsRepository`() = runTest {
        val props = Props(type = "testEventType")

        insertEventUseCase.invoke(props)

        coVerify { eventsRepository.insertOrAbort(props) }
    }

    @Test
    fun `invoke should log error when exception occurs`() = runTest {
        val props = Props(type = "testEventType")
        val exception = RuntimeException("Test Exception")
        coEvery { eventsRepository.insertOrAbort(props) } throws exception

        insertEventUseCase.invoke(props)

        coVerify { logger.error("Inserting event ${props.type} error: $exception") }
    }
}