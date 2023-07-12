package com.walletconnect.modal.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.KoinApplication

class WalletConnectModalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val koinApp: KoinApplication = mockk()
    val koin: Koin = mockk()
    val getWalletsUseCase: GetWalletsUseCaseInterface = mockk()
    val savedStateHandle: SavedStateHandle = mockk()

//    internal lateinit var viewModel = WalletConnectModalViewModel

    @Before
    fun setup() {
        mockkStatic("com.walletconnect.android.internal.common.KoinApplicationKt")
        every { wcKoinApp } returns koinApp
        every { koinApp.koin } returns koin
        every { koin.get<GetWalletsUseCaseInterface>() } returns getWalletsUseCase
    }

    @Test
    fun `should emit WalletConnectModalState without wallets when getWallets return error1`() = runTest {
        val uri = "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        every { savedStateHandle.get<String>(MODAL_URI_ARG) } returns uri
        every { savedStateHandle.get<String>(MODAL_CHAINS_ARG) } returns null

        val viewModel = WalletConnectModalViewModel(savedStateHandle)
        viewModel.modalState.test {
            val state = awaitItem()
            Assert.assertEquals(state, WalletConnectModalState(uri))
        }
        coVerify { getWalletsUseCase.invoke("wcm", null, listOf()) }
    }
}
