package com.walletconnect.web3.modal.ui.navigation.account

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.addTitleArg
import com.walletconnect.web3.modal.ui.routes.account.AccountViewModel
import com.walletconnect.web3.modal.ui.routes.account.chain_redirect.ChainSwitchRedirectRoute

private const val CHAIN_ID_KEY = "chainId"
private const val CHAIN_ID_ARG = "{chainId}"

internal fun Modal.Model.Chain.toChainSwitchPath() = Route.CHAIN_SWITCH_REDIRECT.path + "/${id}&${chainName}"

internal fun NavGraphBuilder.chainSwitchRoute(
    accountViewModel: AccountViewModel
) {
    composable(
        route = Route.CHAIN_SWITCH_REDIRECT.path + "/" + CHAIN_ID_ARG + addTitleArg(),
        arguments = listOf(navArgument(CHAIN_ID_KEY) { type = NavType.StringType } )
    ) { backStackEntry ->
        val chainId = backStackEntry.arguments?.getString(CHAIN_ID_KEY, String.Empty)
        val chain = Web3Modal.chains.find { it.id == chainId }
        chain?.let { ChainSwitchRedirectRoute(accountViewModel = accountViewModel, chain = it) }
    }
}
