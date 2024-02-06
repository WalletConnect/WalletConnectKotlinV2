@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.dapp.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample.dapp.ui.routes.bottom_routes.PairingSelectionRoute
import com.walletconnect.sample.dapp.ui.routes.composable_routes.account.AccountRoute
import com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection.ChainSelectionRoute
import com.walletconnect.sample.dapp.ui.routes.composable_routes.session.SessionRoute
import com.walletconnect.wcmodal.ui.walletConnectModalGraph

@Composable
fun DappSampleNavGraph(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    startDestination: String,
) {
    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = 0.dp,
        scrimColor = Color.Unspecified,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Route.ChainSelection.path) {
                ChainSelectionRoute(navController)
            }
            composable(Route.Session.path, deepLinks = listOf(NavDeepLink("kotlin-dapp-wc://request"))) {
                SessionRoute(navController)
            }
            composable(
                route = Route.Account.path + "/{$accountArg}",
                arguments = listOf(navArgument(accountArg) { type = NavType.StringType })
            ) {
                AccountRoute(navController)
            }
            bottomSheet(Route.ParingSelection.path) {
                PairingSelectionRoute(navController)
            }
            walletConnectModalGraph(navController)
        }
    }
}

const val accountArg= "accountArg"
fun NavController.navigateToAccount(selectedAccount: String) {
    navigate(Route.Account.path + "/$selectedAccount")
}
