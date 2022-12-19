@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.showcase.ui

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
import com.walletconnect.showcase.ui.routes.Route
import com.walletconnect.showcase.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.showcase.ui.theme.ShowcaseTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ShowcaseActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberNavController(bottomSheetNavigator)
            this.navController = navController

            val showcaseViewModel: ShowcaseViewModel = ShowcaseViewModel(application)
            val connectionsViewModel: ConnectionsViewModel = viewModel()

            showcaseViewModel.walletEvents
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

            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val getStartedVisited = sharedPref.getBoolean("get_started_visited", false)
            ShowcaseTheme {
                // Note (Szymon): Due to lack of capacity I didn't implement remembering if user already seen GetStarted route.
                ShowcaseNavGraph(
                    bottomSheetNavigator = bottomSheetNavigator, navController = navController,
                    getStartedVisited = getStartedVisited, showcaseViewModel = showcaseViewModel, connectionsViewModel = connectionsViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }
}
