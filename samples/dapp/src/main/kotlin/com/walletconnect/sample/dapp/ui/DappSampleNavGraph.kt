package com.walletconnect.sample.dapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection.ChainSelectionRoute

@Composable
fun DappSampleNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.ChainSelection.path) {
            ChainSelectionRoute(navController)
        }
    }
}
