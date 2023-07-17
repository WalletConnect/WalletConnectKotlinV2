@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.wcmodal.ui

import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.fragment.dialog
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.wcmodal.ui.navigation.Route

internal const val MODAL_URI_ARG = "modal_uri_arg"
internal const val MODAL_URI_KEY = "uri"
internal const val MODAL_CHAINS_ARG = "modal_chains_arg"
internal const val MODAL_CHAINS_KEY = "chains"
private val MODAL_PATH = Route.WalletConnectModalRoot.path + "?$MODAL_URI_KEY={$MODAL_URI_ARG}&$MODAL_CHAINS_KEY={$MODAL_CHAINS_ARG}"

fun NavGraphBuilder.walletConnectModal() {
    dialog<WalletConnectModalSheet>(route = MODAL_PATH) {
        argument(MODAL_URI_ARG) {
            type = NavType.StringType
        }
        argument(MODAL_CHAINS_ARG) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    }
}

fun NavController.openWalletConnectModal(
    @IdRes id: Int,
    uri: String,
    chains: List<String>? = null
) {
    val bundle = Bundle().apply {
        putString(MODAL_URI_ARG, uri)
        putString(MODAL_CHAINS_KEY, chains?.joinToString())
    }
    navigate(id, bundle)
}

fun NavController.openWalletConnectModal(
    uri: String,
    chains: List<String>? = null
) {
    navigate(buildPath(uri, chains))
}

private fun buildPath(uri: String, chains: List<String>?): String {
    val builder = StringBuilder()
        .append(Route.WalletConnectModalRoot.path)
        .append("?$MODAL_URI_KEY=${Uri.encode(uri)}")
    if (!chains.isNullOrEmpty()) {
        builder.append("&$MODAL_CHAINS_KEY=${chains.joinToString()}")
    }
    return builder.toString()
}

fun NavGraphBuilder.walletConnectModalGraph(navController: NavController) {
    bottomSheet(route = MODAL_PATH) {
        WalletConnectModal(navController = navController)
    }
}
