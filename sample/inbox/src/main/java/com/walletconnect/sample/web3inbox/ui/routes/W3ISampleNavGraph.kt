@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.sample.web3inbox.ui.routes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.walletconnect.sample.web3inbox.ui.routes.select_account.AccountRoute
import com.walletconnect.sample.web3inbox.ui.routes.home.HomeRoute
import com.walletconnect.wcmodal.ui.walletConnectModalGraph

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
fun W3ISampleNavGraph(
    sheetState: ModalBottomSheetState,
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
) {

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = 0.dp,
        scrimColor = Color.Unspecified
    ) {
        NavHost(
            navController = navController,
            startDestination = Route.SelectAccount.path
        ) {
            composable(Route.SelectAccount.path) {
                AccountRoute(navController)
            }
            composable(Route.Home.path + "/{$accountArg}", arguments = listOf(navArgument(accountArg) { type = NavType.StringType })) {
                HomeRoute(navController)
            }
            walletConnectModalGraph(navController)
        }
    }
}

const val accountArg = "accountArg"


fun NavController.navigateToW3I(selectedAccount: String) {
    navigate(Route.Home.path + "/$selectedAccount")
}


fun NavController.navigateToSelectAccount() {
    navigate(Route.SelectAccount.path)
}



