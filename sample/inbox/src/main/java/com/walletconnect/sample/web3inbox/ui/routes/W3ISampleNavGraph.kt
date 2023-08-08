@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.web3inbox.ui.routes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.walletconnect.sample.web3inbox.ui.routes.select_account.AccountRoute
import com.walletconnect.sample.web3inbox.ui.routes.home.HomeRoute
import com.walletconnect.wcmodal.ui.theme.WalletConnectModalTheme
import com.walletconnect.wcmodal.ui.walletConnectModalGraph

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun W3ISampleNavGraph(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
) {
    WalletConnectModalTheme(
        accentColor = MaterialTheme.colors.primary,
        onAccentColor = MaterialTheme.colors.onPrimary
    ) {
        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator,
            sheetShape = RoundedCornerShape(topEnd = 12.dp, topStart = 12.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.SelectAccount.path
            ) {
                composable(Route.SelectAccount.path) {
                    AccountRoute(navController)
                }
                composable(Route.Home.path + "/{$accountArg}", arguments = listOf(navArgument(accountArg) { type = NavType.StringType })) { navBackStackEntry ->
                    HomeRoute(navController, navBackStackEntry.arguments?.getString(accountArg)!!)
                }
                walletConnectModalGraph(navController)
            }
        }
    }
}

const val accountArg = "accountArg"

fun NavController.navigateToW3I(selectedAccount: String) {
    navigate(Route.Home.path + "/$selectedAccount")
}

fun NavController.navigateToSelectAccount() {
    navigate(Route.SelectAccount.path) {
        popUpTo(Route.SelectAccount.path) { inclusive = true }
    }
}
