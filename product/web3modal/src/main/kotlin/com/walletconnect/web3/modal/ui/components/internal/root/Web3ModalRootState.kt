package com.walletconnect.web3.modal.ui.components.internal.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.titleKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
    private val currentDestinationFlow: Flow<NavBackStackEntry>
        get() = navController.currentBackStackEntryFlow

    val canPopUp: Boolean
        get() = !topLevelDestinations.any { it.path == navController.currentDestination?.route }

    val title: StateFlow<String?>
        get() = currentDestinationFlow
            .map { it.arguments?.getString(titleKey) ?: it.destination.toTitle() }
            .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    fun navigateToHelp() {
        navController.navigate(Route.HELP.path)
    }

    fun popUp() {
        navController.popBackStack()
    }
}

private fun NavDestination.toTitle(): String? = Route.values().find { it.path == route }?.title

private val topLevelDestinations = listOf(Route.CONNECT_YOUR_WALLET, Route.SESSION)
