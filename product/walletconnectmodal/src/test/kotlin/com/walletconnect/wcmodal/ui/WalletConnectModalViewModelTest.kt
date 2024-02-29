package com.walletconnect.wcmodal.ui

import app.cash.turbine.test
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.modal.data.model.WalletListing
import com.walletconnect.android.internal.common.modal.domain.usecase.GetInstalledWalletsIdsUseCaseInterface
import com.walletconnect.android.internal.common.modal.domain.usecase.GetSampleWalletsUseCaseInterface
import com.walletconnect.android.internal.common.modal.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.wcmodal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.wcmodal.utils.testWallets
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.KoinApplication

class WalletConnectModalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val koinApp: KoinApplication = mockk()
    private val koin: Koin = mockk()
    private val getWalletsUseCase: GetWalletsUseCaseInterface = mockk()
    private val getRecentWalletUseCase: GetRecentWalletUseCase = mockk()
    private val saveRecentWalletUseCase: SaveRecentWalletUseCase = mockk()
    private val getWalletsAppDataUseCase: GetInstalledWalletsIdsUseCaseInterface = mockk()
    private val getSampleWalletsUseCaseInterface: GetSampleWalletsUseCaseInterface = mockk()
    private val logger: Logger = mockk()

    private val sessionParams = Modal.Params.SessionParams(
        mapOf(
            Pair("eip155", Modal.Model.Namespace.Proposal(chains = listOf("eip155:1"), methods = listOf(), events = listOf()))
        ),
        null,
        null
        )

    @Before
    fun setup() {
        mockkStatic("com.walletconnect.android.internal.common.KoinApplicationKt")
        every { wcKoinApp } returns koinApp
        every { koinApp.koin } returns koin
        every { koin.get<GetWalletsUseCaseInterface>() } returns getWalletsUseCase
        every { koin.get<GetRecentWalletUseCase>() } returns getRecentWalletUseCase
        every { koin.get<SaveRecentWalletUseCase>() } returns saveRecentWalletUseCase
        every { koin.get<GetInstalledWalletsIdsUseCaseInterface>() } returns getWalletsAppDataUseCase
        every { koin.get<GetSampleWalletsUseCaseInterface>() } returns getSampleWalletsUseCaseInterface
        every { koin.get<Logger>() } returns logger
        every { getRecentWalletUseCase() } returns null

        coEvery { getWalletsAppDataUseCase.invoke("wcm") } returns listOf()
        WalletConnectModal.setSessionParams(sessionParams)
    }

    @After
    fun after() {
        unmockkObject(CoreClient)
        unmockkObject(WalletConnectModal)
    }

    @Test
    fun `should emit Error state when fetch initial wallets fails`() = runTest {
        every { logger.error(any<String>()) } answers {}
        every { logger.error(any<Throwable>()) } answers {}

        val viewModel = WalletConnectModalViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            Assert.assertTrue(state is UiState.Error)
        }
    }

    @Test
    fun `should emit Success state when fetch initial wallets with success`() = runTest {
        val response = WalletListing(1, testWallets.size, testWallets)
        coEvery { getWalletsUseCase(any(), any(), any(), any(), any()) } returns response
        coEvery { getSampleWalletsUseCaseInterface() } returns listOf()

        val viewModel = WalletConnectModalViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            println(state)
            Assert.assertTrue(state is UiState.Success)
        }
    }
}
