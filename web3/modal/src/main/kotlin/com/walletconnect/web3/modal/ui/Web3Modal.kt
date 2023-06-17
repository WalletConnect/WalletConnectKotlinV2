@file:OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class
)

package com.walletconnect.web3.modal.ui
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.fragment.dialog
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.web3.modal.domain.configuration.CONFIG_ARG
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.domain.configuration.asArg
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalComponent
import com.walletconnect.web3.modal.ui.navigation.Route

fun NavGraphBuilder.web3Modal() {
    dialog<Web3ModalSheet>("${Route.Web3Modal.path}/{$CONFIG_ARG}") {
        argument(CONFIG_ARG) {
            type = NavType.StringType
        }
    }
}

fun NavController.navigateToWeb3modal(
    @IdRes id: Int,
    config: Config,
) {
    val bundle = Bundle().apply {
        putString(CONFIG_ARG, config.asArg())
    }
    navigate(id, bundle)
}
fun NavController.navigateToWeb3Modal(config: Config) {
    navigate(Route.Web3Modal.path + "/${config.asArg()}")
}

fun NavGraphBuilder.web3ModalGraph(
    navController: NavController,
    sheetState: ModalBottomSheetState
) {
    bottomSheet(
        route = Route.Web3Modal.path + "/{$CONFIG_ARG}",
    ) {
        Web3Modal(navController = navController, sheetState = sheetState)
    }
}

@Composable
internal fun Web3Modal(
    navController: NavController,
    sheetState: ModalBottomSheetState
) {
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
            if (navController.currentDestination?.route?.contains(Route.Web3Modal.path) == true) {
                navController.popBackStack()
            }
        }
    }

    Web3ModalComponent(
        closeModal = { navController.popBackStack() }
    )
}
