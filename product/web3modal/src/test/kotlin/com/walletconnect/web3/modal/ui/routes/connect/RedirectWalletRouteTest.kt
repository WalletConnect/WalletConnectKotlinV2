package com.walletconnect.web3.modal.ui.routes.connect

import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.PlatformTab
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.routes.connect.redirect.RedirectState
import com.walletconnect.web3.modal.ui.routes.connect.redirect.RedirectWalletScreen
import com.walletconnect.web3.modal.utils.MainDispatcherRule
import com.walletconnect.web3.modal.utils.ScreenShotTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("This test is not working on CI for Sonar only")
internal class RedirectWalletRouteTest : ScreenShotTest("connect/${Route.REDIRECT.path}") {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val wallet = testWallets.first()

    @Test
    fun `test RedirectWalletRoute with Loading state in LightMode `() = runRouteScreenShotTest(
        title = wallet.name
    ) {
        val state = RedirectState.Loading
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }

    @Test
    fun `test RedirectWalletRoute with Loading state in DarkMode `() = runRouteScreenShotTest(
        title = wallet.name,
        nightMode = NightMode.NIGHT
    ) {
        val state = RedirectState.Loading
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }

    @Test
    fun `test RedirectWalletRoute with Loading state in Landscape `() = runRouteScreenShotTest(
        title = wallet.name,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        val state = RedirectState.Loading
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }

    @Test
    fun `test RedirectWalletRoute with NotDetected state `() = runRouteScreenShotTest(
        title = wallet.name
    ) {
        val state = RedirectState.NotDetected
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }


    @Test
    fun `test RedirectWalletRoute with NotDetected state in DarkMode `() = runRouteScreenShotTest(
        title = wallet.name,
        nightMode = NightMode.NIGHT
    ) {
        val state = RedirectState.NotDetected
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }

    @Test
    fun `test RedirectWalletRoute with NotDetected state in Landscape `() = runRouteScreenShotTest(
        title = wallet.name,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        val state = RedirectState.NotDetected
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }

    @Test
    fun `test RedirectWalletRoute with Reject state `() = runRouteScreenShotTest(
        title = wallet.name
    ) {
        val state = RedirectState.Reject
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }


    @Test
    fun `test RedirectWalletRoute with Reject state in DarkMode `() = runRouteScreenShotTest(
        title = wallet.name,
        nightMode = NightMode.NIGHT
    ) {
        val state = RedirectState.Reject
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }

    @Test
    fun `test RedirectWalletRoute with Reject state in Landscape `() = runRouteScreenShotTest(
        title = wallet.name,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        val state = RedirectState.Reject
        val platformTab = PlatformTab.MOBILE
        RedirectWalletScreen(state, platformTab, {}, wallet, {}, {}, {}, {})
    }
}
