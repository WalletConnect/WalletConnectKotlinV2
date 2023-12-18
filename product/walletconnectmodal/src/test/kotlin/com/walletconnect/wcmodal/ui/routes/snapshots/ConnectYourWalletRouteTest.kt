package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.navigation.NavController
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.WalletConnectModalViewModel
import com.walletconnect.wcmodal.ui.routes.connect_wallet.ConnectYourWalletRoute
import com.walletconnect.wcmodal.utils.ScreenShotTest
import com.walletconnect.wcmodal.utils.testWallets
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.KoinApplication

class ConnectYourWalletRouteTest: ScreenShotTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navController: NavController = mockk()
    private val viewModel: WalletConnectModalViewModel = mockk()
    private val koinApp: KoinApplication = mockk()
    private val koin: Koin = mockk()

    @Before
    fun setup() {
        mockkStatic("com.walletconnect.android.internal.common.KoinApplicationKt")
        every { wcKoinApp } returns koinApp
        every { koinApp.koin } returns koin
        every { koin.get<ProjectId>() } returns ProjectId("test-projectId")
        every { viewModel.fetchInitialWallets() } returns Unit
    }

    @Test
    fun `test ConnectYourWalletRoute in LightMode with empty list of wallets`() = runScreenShotTest {
        every { viewModel.uiState.value } returns UiState.Success(listOf())
        ConnectYourWalletRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWalletRoute in DarkMode with empty list of wallets`() = runScreenShotTest(isDarkMode = true) {
        every { viewModel.uiState.value } returns UiState.Success(listOf())
        ConnectYourWalletRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWalletRoute in LightMode with 3 wallets`() = runScreenShotTest {
        every { viewModel.uiState.value } returns UiState.Success(testWallets.take(3))
        ConnectYourWalletRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWalletRoute in DarkMode with 3 wallets`() = runScreenShotTest(isDarkMode = true) {
        every { viewModel.uiState.value } returns UiState.Success(testWallets.take(3))
        ConnectYourWalletRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWalletRoute in LightMode with whole wallets`() = runScreenShotTest {
        every { viewModel.uiState.value } returns UiState.Success(testWallets)
        ConnectYourWalletRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWalletRoute in DarkMode with whole wallets`() = runScreenShotTest(isDarkMode = true) {
        every { viewModel.uiState.value } returns UiState.Success(testWallets)
        ConnectYourWalletRoute(navController = navController, viewModel = viewModel)
    }
}
