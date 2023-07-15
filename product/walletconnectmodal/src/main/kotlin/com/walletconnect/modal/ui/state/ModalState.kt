@file:OptIn(ExperimentalCoroutinesApi::class)

package com.walletconnect.modal.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.walletconnect.modal.ui.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@Composable
fun rememberModalState(
    navController: NavController
): ModalState = remember(navController) {
    ModalState(navController)
}

class ModalState(
    private val navController: NavController
) {
    val isOpen: Boolean
        get() = navController.currentDestination.isModal()

    val isOpenFlow
        get() = navController.currentBackStackEntryFlow.mapLatest { it.destination.isModal() }

    fun closeModal() {
        if(isOpen) {
            navController.popBackStack()
        }
    }

    private fun NavDestination?.isModal() =
        this?.route?.startsWith("${Route.WalletConnectModalRoot.path}?") ?: false
}