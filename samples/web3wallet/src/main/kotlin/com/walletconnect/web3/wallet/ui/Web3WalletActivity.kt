@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.web3.wallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.ui.routes.Route
import com.walletconnect.web3.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.web3.wallet.ui.theme.Web3WalletTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class Web3WalletActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val web3walletViewModel: Web3WalletViewModel = Web3WalletViewModel()
        val connectionsViewModel: ConnectionsViewModel = ConnectionsViewModel()

        web3walletViewModel.walletEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is SignEvent.SessionProposal -> navController.navigate(Route.SessionProposal.path)
                    is SignEvent.SessionRequest -> navController.navigate(Route.SessionRequest.path)
                    is SignEvent.Disconnect -> {
                        connectionsViewModel.refreshConnections()
                        navController.navigate(Route.Connections.path)
                    }
                    is AuthEvent.OnRequest -> navController.navigate(Route.AuthRequest.path)

                    else -> Unit
                }
            }
            .launchIn(lifecycleScope)


        connectionsViewModel.coreEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is CoreEvent.Disconnect -> {
                        connectionsViewModel.refreshConnections()
                        navController.navigate(Route.Connections.path)
                    }
                    else -> Unit
                }
            }
            .launchIn(lifecycleScope)

        setContent {
            val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberNavController(bottomSheetNavigator)
            this.navController = navController

            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val getStartedVisited = sharedPref.getBoolean("get_started_visited", false)
            Web3WalletTheme {
                // Note (Szymon): Due to lack of capacity I didn't implement remembering if user already seen GetStarted route.
                Web3WalletNavGraph(
                    bottomSheetNavigator = bottomSheetNavigator, navController = navController,
                    getStartedVisited = getStartedVisited, web3walletViewModel = web3walletViewModel, connectionsViewModel = connectionsViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }
}
