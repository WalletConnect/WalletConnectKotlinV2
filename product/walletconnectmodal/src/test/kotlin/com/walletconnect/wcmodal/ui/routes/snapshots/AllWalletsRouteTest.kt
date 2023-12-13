package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.navigation.NavController
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.wcmodal.domain.dataStore.WalletsData
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.WalletConnectModalViewModel
import com.walletconnect.wcmodal.ui.routes.all_wallets.AllWalletsRoute
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

class AllWalletsRouteTest: ScreenShotTest() {

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
        every { viewModel.searchPhrase } returns ""
    }

    @Test
    fun `test AllWalletsRoute in LightMode with empty list of wallets`() = runScreenShotTest {
        every { viewModel.walletsState.value } returns WalletsData(listOf())
        AllWalletsRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with empty list of wallets`() = runScreenShotTest(isDarkMode = true) {
        every { viewModel.walletsState.value } returns WalletsData(listOf())
        AllWalletsRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test AllWalletsRoute in LightMode with 3 wallets`() = runScreenShotTest {
        every { viewModel.walletsState.value } returns WalletsData(testWallets.take(3))
        AllWalletsRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with 3 wallets`() = runScreenShotTest(isDarkMode = true) {
        every { viewModel.walletsState.value } returns WalletsData(testWallets.take(3))
        AllWalletsRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test AllWalletsRoute in LightMode whole wallets list`() = runScreenShotTest {
        every { viewModel.walletsState.value } returns WalletsData(testWallets)
        AllWalletsRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with whole wallets list`() = runScreenShotTest(isDarkMode = true) {
        every { viewModel.walletsState.value } returns WalletsData(testWallets)
        AllWalletsRoute(navController = navController, viewModel = viewModel)
    }
}
