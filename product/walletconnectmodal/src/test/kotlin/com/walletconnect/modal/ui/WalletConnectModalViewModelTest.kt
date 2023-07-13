package com.walletconnect.modal.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.modal.client.WalletConnectModal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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

    @Before
    fun setup() {
        mockkStatic("com.walletconnect.android.internal.common.KoinApplicationKt")
        every { wcKoinApp } returns koinApp
        every { koinApp.koin } returns koin
        every { koin.get<GetWalletsUseCaseInterface>() } returns getWalletsUseCase
    }

    @Test
    fun `should call getWallets with parsed chains argument`() = runTest {
        val uri = "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        val chains = "eip155:1, eip155:2"
        every { savedStateHandle.get<String>(MODAL_URI_ARG) } returns uri
        every { savedStateHandle.get<String>(MODAL_CHAINS_ARG) } returns chains
        val viewModel = WalletConnectModalViewModel(savedStateHandle)

        coVerify { getWalletsUseCase("wcm", chains, listOf()) }
    }

    @Test
    fun `should emit WalletConnectModalState without wallets when getWallets return error`() = runTest {
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

    @Test
    fun `should emit WalletConnectModalState with wallets when getWallets return success`() = runTest {
        val wallets = listOf(
            Wallet("id1", "MetaMask", "", null, null, null),
            Wallet("id2", "TrustWallet", "", null, null, null),
            Wallet("id3", "Safe", "", null, null, null),
            Wallet("id4", "Rainbow", "", null, null, null),
        )
        val uri = "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        every { savedStateHandle.get<String>(MODAL_URI_ARG) } returns uri
        every { savedStateHandle.get<String>(MODAL_CHAINS_ARG) } returns null
        coEvery { getWalletsUseCase.invoke(sdkType = any(), null, listOf(), null) } returns wallets

        val viewModel = WalletConnectModalViewModel(savedStateHandle)
        viewModel.modalState.test {
            val state = awaitItem()
            Assert.assertEquals(state, WalletConnectModalState(uri, wallets))
        }

        coVerify { getWalletsUseCase.invoke("wcm", null, listOf()) }
    }

    @Test
    fun `should emit WalletConnectModalState with correct order when call getWallets with recommended wallets id`() = runTest {
        WalletConnectModal.recommendedWalletsIds = listOf("id5")
        val wallets = listOf(
            Wallet("id1", "MetaMask", "", null, null, null),
            Wallet("id2", "TrustWallet", "", null, null, null),
            Wallet("id3", "Safe", "", null, null, null),
            Wallet("id4", "Rainbow", "", null, null, null),
        )
        val recommendedWallet = Wallet("id5", "Zerion", "", null, null, null)
        val recommendedWallets = listOf(recommendedWallet)
        val uri = "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        every { savedStateHandle.get<String>(MODAL_URI_ARG) } returns uri
        every { savedStateHandle.get<String>(MODAL_CHAINS_ARG) } returns null
        coEvery { getWalletsUseCase.invoke(sdkType = any(), null, listOf(), null) } returns wallets
        coEvery { getWalletsUseCase.invoke(sdkType = any(), null, listOf(), listOf("id5")) } returns recommendedWallets


        val viewModel = WalletConnectModalViewModel(savedStateHandle)

        viewModel.modalState.test {
            val state = awaitItem()
            Assert.assertEquals(state?.wallets?.first(), recommendedWallet)
            Assert.assertEquals(state?.wallets?.size, 5)
        }

        coVerify { getWalletsUseCase.invoke("wcm", null, listOf()) }
        coVerify { getWalletsUseCase.invoke("wcm", null, listOf(), listOf("id5")) }
        WalletConnectModal.recommendedWalletsIds = listOf()
    }
}
