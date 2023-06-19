@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
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
import com.walletconnect.sample.dapp.ui.routes.dialog_routes.MessageDialogRoute
import com.walletconnect.web3.modal.ui.web3ModalGraph

@Composable
fun DappSampleNavGraph(
    sheetState: ModalBottomSheetState,
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    startDestination: String,
) {
    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = 0.dp,
        scrimColor = Color.Unspecified
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
            dialog(
                route = Route.MessageDialog.path + "/{$messageArg}",
                arguments = listOf(navArgument(messageArg) { type = NavType.Companion.StringType }),
                dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                MessageDialogRoute(
                    navController = navController,
                    message = checkNotNull(it.arguments?.getString(messageArg))
                )
            }
            web3ModalGraph(navController, sheetState)
        }
    }
}

const val accountArg= "accountArg"
const val messageArg = "messageArg"
fun NavController.openMessageDialog(message: String) {
    navigate(Route.MessageDialog.path + "/$message")
}

fun NavController.navigateToAccount(selectedAccount: String) {
    navigate(Route.Account.path + "/$selectedAccount")
}
