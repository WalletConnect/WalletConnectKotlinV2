@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection.ChainSelectionRoute
import com.walletconnect.sample.dapp.web3modal.ui.Web3Modal

@Composable
fun DappSampleNavGraph(
    sheetState: ModalBottomSheetState,
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = 0.dp,
        scrimColor = Color.Unspecified
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Route.ChainSelection.path) {
                ChainSelectionRoute(navController)
            }
            bottomSheet(Route.Web3Modal.path) {
                Web3Modal(sheetState)
            }
        }
    }
}
