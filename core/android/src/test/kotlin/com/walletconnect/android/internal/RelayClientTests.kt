package com.walletconnect.android.internal

import app.cash.turbine.test
import com.tinder.scarlet.WebSocket
import com.walletconnect.android.internal.common.connection.ConnectivityState
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.WSSConnectionState
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.util.Logger
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class RelayClientTests {
	private lateinit var relayClient: RelayClient
	private val mockRelayService = mockk<RelayService>(relaxed = true)
	private val mockLogger = mockk<Logger>(relaxed = true)
	private val mockConnectionController = mockk<ConnectionController>(relaxed = true)
	private val mockNetworkState = mockk<ConnectivityState>(relaxed = true)
	private val testDispatcher = StandardTestDispatcher()
	private val testScope = TestScope(testDispatcher)
	private var koinApp = mockk<KoinApplication>()

	@Before
	fun setup() {
		koinApp = startKoin {
			modules(module {
				single(named(AndroidCommonDITags.RELAY_SERVICE)) { mockRelayService }
				single(named(AndroidCommonDITags.LOGGER)) { mockLogger }
				single(named(AndroidCommonDITags.CONNECTION_CONTROLLER)) { mockConnectionController }
				single(named(AndroidCommonDITags.CONNECTIVITY_STATE)) { mockNetworkState }
			})
		}

		relayClient = RelayClient(koinApp = koinApp).apply {
			scope = testScope
		}

		mockkStatic(AndroidCommonDITags::class)
	}

	@After
	fun tearDown() {
		stopKoin()
		clearAllMocks()
	}

	@Test
	fun `initialize should setup components and observe results`() = testScope.runTest {
		every { mockRelayService.observeWebSocketEvent() } returns flow {
			emit(WebSocket.Event.OnConnectionFailed(Throwable("Network failure")))
			emit(WebSocket.Event.OnConnectionOpened("Opened"))
			emit(WebSocket.Event.OnConnectionFailed(Throwable("Network failure2")))
		}

		relayClient.initialize { error ->
			assertEquals(
				"Error while connecting, please check your Internet connection or contact support: java.lang.Throwable: Network failure",
				error.message
			)
			scope.coroutineContext.cancelChildren()
		}

		advanceUntilIdle()
		coVerify { mockRelayService.observeWebSocketEvent() }
	}

	@Test
	fun `monitorConnectionState should handle connection states correctly`() = testScope.runTest {
		val wssConnectionState = relayClient.wssConnectionState as MutableStateFlow
		every { mockRelayService.observeWebSocketEvent() } returns flow {
			emit(WebSocket.Event.OnConnectionOpened("Opened"))
		}

		relayClient.initialize {}
		advanceUntilIdle()
		coVerify { mockRelayService.observeWebSocketEvent() }

		wssConnectionState
			.test {
				assertEquals(WSSConnectionState.Disconnected(), awaitItem())
				assertEquals(WSSConnectionState.Connected, awaitItem())
				cancelAndIgnoreRemainingEvents()
			}
	}
}