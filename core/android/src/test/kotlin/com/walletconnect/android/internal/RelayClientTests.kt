package com.walletconnect.android.internal

import com.tinder.scarlet.WebSocket
import com.walletconnect.android.internal.common.connection.ConnectivityState
import com.walletconnect.android.internal.common.connection.DefaultConnectionLifecycle
import com.walletconnect.android.internal.common.connection.ManualConnectionLifecycle
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.util.Logger
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
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
	private val defaultConnectionLifecycleMock = mockk<DefaultConnectionLifecycle>(relaxed = true)
	private val manualConnectionLifecycleMock = mockk<ManualConnectionLifecycle>(relaxed = true)
	private val mockLogger = mockk<Logger>(relaxed = true)
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
				single(named(AndroidCommonDITags.CONNECTIVITY_STATE)) { mockNetworkState }
				single(named(AndroidCommonDITags.MANUAL_CONNECTION_LIFECYCLE)) { manualConnectionLifecycleMock }
				single(named(AndroidCommonDITags.DEFAULT_CONNECTION_LIFECYCLE)) { defaultConnectionLifecycleMock }
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

		relayClient.initialize(ConnectionType.MANUAL) { error ->
			assertEquals(
				"Error while connecting, please check your Internet connection or contact support: Network failure",
				error.message
			)
			scope.coroutineContext.cancelChildren()
		}

		advanceUntilIdle()
		coVerify { mockRelayService.observeWebSocketEvent() }
	}

	//TODO: Cannot make it to run - revisit
//	@Test
//	fun `monitorConnectionState should handle connection states correctly`() = testScope.runTest {
//		val wssConnectionState = relayClient.wssConnectionState as MutableStateFlow
//		every { mockRelayService.observeWebSocketEvent() } returns flow {
//			emit(WebSocket.Event.OnConnectionOpened("Opened"))
//		}
//
//		relayClient.initialize {}
//
//		coVerify { mockRelayService.observeWebSocketEvent() }
//
//		advanceUntilIdle()
//
//		wssConnectionState
//			.test {
//				assertEquals(WSSConnectionState.Disconnected.ConnectionClosed(), awaitItem().also { println("1: $it") })
//				assertEquals(WSSConnectionState.Connected, awaitItem().also { println("2: $it") })
//
//				advanceUntilIdle()
//				this.coroutineContext.cancelChildren()
//				this.cancelAndIgnoreRemainingEvents()
//				this.ensureAllEventsConsumed()
//			}
//	}
}