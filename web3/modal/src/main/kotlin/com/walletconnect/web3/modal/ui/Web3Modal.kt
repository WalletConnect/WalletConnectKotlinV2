@file:OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class
)

package com.walletconnect.web3.modal.ui

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.*
import androidx.navigation.*
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.web3.modal.domain.configuration.CONFIGURATION
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.domain.configuration.Web3ModalConfigSerializer
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalComponent
import com.walletconnect.web3.modal.ui.navigation.Route
import kotlinx.coroutines.launch

fun NavController.navigateToWeb3modal(
    @IdRes id: Int,
    config: Config,
) {
    val bundle = Bundle().apply {
        putString(CONFIGURATION, config.parse())
    }
    navigate(id, bundle)
}

fun NavController.navigateToWeb3Modal(config: Config) {
    navigate(Route.Web3Modal.path + "/${config.parse()}")
}

fun NavGraphBuilder.web3ModalGraph(sheetState: ModalBottomSheetState) {
    bottomSheet(
        route = Route.Web3Modal.path + "/{$CONFIGURATION}",
    ) {
        Web3Modal(sheetState = sheetState)
    }
}

@Composable
internal fun Web3Modal(
    sheetState: ModalBottomSheetState
) {
    val coroutineScope = rememberCoroutineScope()

    Web3ModalComponent(
        closeModal = { coroutineScope.launch { sheetState.hide() } }
    )
}

private fun Config.parse() = Web3ModalConfigSerializer.serialize(this)