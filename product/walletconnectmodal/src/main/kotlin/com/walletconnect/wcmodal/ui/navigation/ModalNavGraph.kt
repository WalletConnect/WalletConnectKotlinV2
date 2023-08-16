package com.walletconnect.wcmodal.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    state: WalletConnectModalState.Connect,
    modifier: Modifier = Modifier,
    updateRecentWalletId: (String) -> Unit,
    retry: (() -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Route.ConnectYourWallet.path,
        modifier = modifier,
        enterTransition = { fadeIn(tween()) },
        popExitTransition = { fadeOut(tween()) },
        exitTransition = { fadeOut(tween()) },
        popEnterTransition = { fadeIn(tween()) }
    ) {
        composable(route = Route.ConnectYourWallet.path) {
            ConnectYourWalletRoute(navController = navController, wallets = state.wallets)
        }
        composable(route = Route.ScanQRCode.path) {
            ScanQRCodeRoute(navController = navController, uri = state.uri)
        }
        composable(route = Route.Help.path) {
            HelpRoute(navController = navController)
        }
        composable(route = Route.AllWallets.path) {
            AllWalletsRoute(navController = navController, wallets = state.wallets)
        }
        composable(route = Route.GetAWallet.path) {
            GetAWalletRoute(navController = navController, wallets = state.wallets)
        }
        composable(
            route = Route.OnHold.path + "/" + Route.OnHold.walletIdArg,
            arguments = listOf(navArgument(Route.OnHold.walletIdKey) { type = NavType.StringType })
        ) { backStackEntry ->
            val wallet = state.wallets.find { it.id == backStackEntry.arguments?.getString(Route.OnHold.walletIdKey, String.Empty) }!!
            RedirectOnHoldScreen(navController = navController, uri = state.uri, wallet = wallet, retry = retry).also { updateRecentWalletId(wallet.id) }
        }
    }
}
