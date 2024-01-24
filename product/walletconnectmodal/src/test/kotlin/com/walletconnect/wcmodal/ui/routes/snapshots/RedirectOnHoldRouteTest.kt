package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.navigation.NavController
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.WalletConnectModalViewModel
import com.walletconnect.wcmodal.ui.routes.on_hold.RedirectOnHoldScreen
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

class RedirectOnHoldRouteTest: ScreenShotTest() {

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
    fun `test RedirectOnHoldRoute in LightMode`() = runScreenShotTest {
        RedirectOnHoldScreen(navController = navController, wallet = testWallets.first(), viewModel = viewModel)
    }

    @Test
    fun `test RedirectOnHoldRoute in DarkMode`() = runScreenShotTest(isDarkMode = true) {
        RedirectOnHoldScreen(navController = navController, wallet = testWallets.first(), viewModel = viewModel)
    }
}