package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.SendEventUseCase
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import retrofit2.Response

class SendEventUseCaseTest : KoinTest {
    private val pulseService: PulseService = mockk()
    private val logger: Logger = mockk()
    private val bundleId: String = "testBundleId"
    private lateinit var useCase: SendEventUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val testModule: Module = module {
        single(named(AndroidCommonDITags.ENABLE_WEB_3_MODAL_ANALYTICS)) { true }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val app = startKoin { modules(testModule) }
        useCase = SendEventUseCase(pulseService, logger, bundleId)
        wcKoinApp = app
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `send should log and send event when analytics is enabled and response is successful`() = runTest(testDispatcher) {
        val props = Props(type = "testEvent")
        val sdkType = SDKType.WEB3MODAL
        val event = Event(props = props, bundleId = bundleId)

        coEvery { pulseService.sendEvent(body = any(), sdkType = any()) } returns Response.success(Unit)
        every { logger.log("Event sent successfully: ${event.props.type}") } just Runs

        useCase.send(props, sdkType, event.timestamp, event.eventId)

        advanceUntilIdle()

        coVerify { pulseService.sendEvent(body = event, sdkType = sdkType.type) }
        verify { logger.log("Event sent successfully: ${event.props.type}") }
    }
}