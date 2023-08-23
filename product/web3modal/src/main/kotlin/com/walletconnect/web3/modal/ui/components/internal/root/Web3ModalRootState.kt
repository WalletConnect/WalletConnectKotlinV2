package com.walletconnect.web3.modal.ui.components.internal.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.getTitleArg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
internal fun rememberWeb3ModalRootState(
    coroutineScope: CoroutineScope,
    navController: NavController
): Web3ModalRootState {
    return remember(coroutineScope, navController) {
        Web3ModalRootState(coroutineScope, navController)
    }
}

internal class Web3ModalRootState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController
) {
    val currentDestinationFlow: Flow<NavBackStackEntry>
        get() = navController.currentBackStackEntryFlow

    val canPopUp: Boolean
        get() = !topLevelDestinations.any { it.path == navController.currentDestination?.route }

    val title: Flow<String?>
        get() = currentDestinationFlow
            .map { it.getTitleArg() ?: it.destination.toTitle() }

    fun navigateToHelp() {
        navController.navigate(Route.HELP.path)
    }

    fun popUp() {
        navController.popBackStack()
    }
}

private fun NavDestination.toTitle(): String? = Route.values().find { it.path == route }?.title

private val topLevelDestinations = listOf(Route.CONNECT_YOUR_WALLET, Route.SESSION)
