package com.walletconnect.wcmodal.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.walletconnect.util.Empty
import com.walletconnect.wcmodal.ui.WalletConnectModalState
import com.walletconnect.wcmodal.ui.routes.all_wallets.AllWalletsRoute
import com.walletconnect.wcmodal.ui.routes.connect_wallet.ConnectYourWalletRoute
import com.walletconnect.wcmodal.ui.routes.get_wallet.GetAWalletRoute
import com.walletconnect.wcmodal.ui.routes.help.HelpRoute
import com.walletconnect.wcmodal.ui.routes.on_hold.RedirectOnHoldScreen
import com.walletconnect.wcmodal.ui.routes.scan_code.ScanQRCodeRoute

@Composable
internal fun ModalNavGraph(
    navController: NavHostController,
    state: WalletConnectModalState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.ConnectYourWallet.path,
        modifier = modifier
    ) {
        animatedComposable(route = Route.ConnectYourWallet.path) {
            ConnectYourWalletRoute(navController = navController, wallets = state.wallets)
        }
        animatedComposable(route = Route.ScanQRCode.path) {
            ScanQRCodeRoute(navController = navController, uri = state.uri)
        }
        animatedComposable(route = Route.Help.path) {
            HelpRoute(navController = navController)
        }
        animatedComposable(route = Route.AllWallets.path) {
            AllWalletsRoute(navController = navController, wallets = state.wallets)
        }
        animatedComposable(route = Route.GetAWallet.path) {
            GetAWalletRoute(navController = navController, wallets = state.wallets)
        }
        animatedComposable(
            route = Route.OnHold.path + "/" + Route.OnHold.walletIdArg,
            arguments = listOf(navArgument(Route.OnHold.walletIdKey) { type = NavType.StringType })
        ) { backStackEntry ->
            val wallet = state.wallets.find { it.id == backStackEntry.arguments?.getString(Route.OnHold.walletIdKey, String.Empty) }!!
            RedirectOnHoldScreen(navController = navController, uri = state.uri, wallet = wallet)
        }
    }
}

internal fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        content = { content(it) }
    )
}