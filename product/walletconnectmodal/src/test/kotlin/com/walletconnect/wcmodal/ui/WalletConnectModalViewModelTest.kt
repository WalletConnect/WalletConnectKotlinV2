package com.walletconnect.wcmodal.ui

import app.cash.turbine.test
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.wcmodal.domain.usecase.SaveRecentWalletUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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

    val koinApp: KoinApplication = mockk()
    val koin: Koin = mockk()
    val getWalletsUseCase: GetWalletsUseCaseInterface = mockk()
    val getRecentWalletUseCase: GetRecentWalletUseCase = mockk()
    val saveRecentWalletUseCase: SaveRecentWalletUseCase = mockk()
    val logger: Logger = mockk()

    private val uri = "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
    private val wallets = listOf(Wallet("id1", "MetaMask", "", null, null, null), Wallet("id2", "TrustWallet", "", null, null, null), Wallet("id3", "Safe", "", null, null, null), Wallet("id4", "Rainbow", "", null, null, null),)
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
        every { koin.get<Logger>() } returns logger
        every { getRecentWalletUseCase() } returns null
        WalletConnectModal.setSessionParams(sessionParams)
    }

    @After
    fun after() {
        unmockkObject(CoreClient)
        unmockkObject(WalletConnectModal)
    }

    private fun mockkPairing() {
        mockkObject(CoreClient)
        val pairing: Core.Model.Pairing = mockk()
        val pairingInterface: PairingInterface = mockk()
        every { CoreClient.Pairing } returns pairingInterface
        every { pairingInterface.create(any()) } returns pairing
        every { pairing.uri } returns uri
    }

    private fun mockkConnect() {
        mockkObject(WalletConnectModal)
        every { WalletConnectModal.connect(any(), any(), any()) } answers { secondArg<() -> Unit>().invoke() }
    }

    @Test
    fun `should emit WalletConnectModalState Error state when pairing throw exception`() = runTest {
        every { logger.error(any<String>()) } answers {}
        every { logger.error(any<Throwable>()) } answers {}
        val viewModel = WalletConnectModalViewModel()
        viewModel.modalState.test {
            val state = awaitItem()
            Assert.assertTrue(state is WalletConnectModalState.Error)
        }
    }

    @Test
    fun `should emit WalletConnectModalState Connect when connect and fetch wallets`() = runTest {
        mockkPairing()
        mockkConnect()
        coEvery { getWalletsUseCase(any(), any(), any(), any()) } returns wallets

        val viewModel = WalletConnectModalViewModel()
        viewModel.modalState.test {
            val state = awaitItem()
            Assert.assertTrue(state is WalletConnectModalState.Connect)
        }
    }

    @Test
    fun `should call getWallet on WalletConnectModalViewModel without recommended wallets`() = runTest {
        mockkPairing()
        mockkConnect()
        coEvery { getWalletsUseCase(any(), any(), any(), any()) } returns wallets

        WalletConnectModalViewModel()
        coVerify { getWalletsUseCase.invoke("wcm", "eip155:1", listOf()) }
    }

    @Test
    fun `should call getWallet on WalletConnectModalViewModel with recommended wallets`() = runTest {
        mockkPairing()
        mockkConnect()

        WalletConnectModal.recommendedWalletsIds = listOf("id5")
        val recommendedWallet = Wallet("id5", "Zerion", "", null, null, null)
        val recommendedWallets = listOf(recommendedWallet)
        coEvery { getWalletsUseCase.invoke(sdkType = any(), any(), listOf(), null) } returns wallets
        coEvery { getWalletsUseCase.invoke(sdkType = any(), any(), listOf(), listOf("id5")) } returns recommendedWallets

        WalletConnectModalViewModel()

        coVerify { getWalletsUseCase.invoke("wcm", "eip155:1", listOf(), null) }
        coVerify { getWalletsUseCase.invoke("wcm", "eip155:1", listOf(), listOf("id5")) }
        WalletConnectModal.recommendedWalletsIds = listOf()
    }

    @Test
    fun `should move recent wallet to first position after updateRecentWalletId is called`() = runTest {
        mockkPairing()
        mockkConnect()
        every { saveRecentWalletUseCase.invoke(any()) } returns Unit
        coEvery { getWalletsUseCase.invoke(sdkType = any(), any(), listOf(), null) } returns wallets

        val viewModel = WalletConnectModalViewModel()

        viewModel.modalState.test {
            val state = awaitItem( )as WalletConnectModalState.Connect
            Assert.assertEquals(state.wallets.size, 4)
            Assert.assertEquals(state.wallets.first().id, "id1")
        }

        viewModel.updateRecentWalletId("id4")

        viewModel.modalState.test {
            val state = awaitItem() as WalletConnectModalState.Connect
            Assert.assertEquals(state.wallets.size, 4)
            Assert.assertEquals(state.wallets.first().id, "id4")
        }
    }
}
