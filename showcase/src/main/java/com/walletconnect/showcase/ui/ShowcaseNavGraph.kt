@file:OptIn(ExperimentalComposeUiApi::class)

package com.walletconnect.showcase.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.*
import com.walletconnect.showcase.ui.routes.Route
import com.walletconnect.showcase.ui.routes.bottomsheet_routes.scan_uri.ScanUriRoute
import com.walletconnect.showcase.ui.routes.composable_routes.connection_details.ConnectionDetailsRoute
import com.walletconnect.showcase.ui.routes.composable_routes.connections.ConnectionsRoute
import com.walletconnect.showcase.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.showcase.ui.routes.composable_routes.get_started.GetStartedRoute
import com.walletconnect.showcase.ui.routes.dialog_routes.auth_request.AuthRequestRoute
import com.walletconnect.showcase.ui.routes.dialog_routes.paste_uri.PasteUriRoute
import com.walletconnect.showcase.ui.routes.dialog_routes.session_proposal.SessionProposalRoute
import com.walletconnect.showcase.ui.routes.dialog_routes.session_request.SessionRequestRoute
import com.walletconnect.showcase.ui.routes.dialog_routes.snackbar_message.SnackbarMessageRoute

@ExperimentalMaterialNavigationApi
@Composable
fun ShowcaseNavGraph(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    showcaseViewModel: ShowcaseViewModel,
    connectionsViewModel: ConnectionsViewModel,
    getStartedVisited: Boolean,
    startDestination: String = if (getStartedVisited) Route.Connections.path else Route.GetStarted.path,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = Color.Transparent, sheetElevation = 0.dp,
        scrimColor = Color.Unspecified
    ) {
        Scaffold(
            content = { innerPadding ->
                val sheetState = remember { bottomSheetNavigator.navigatorSheetState }
                NavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    composable(Route.GetStarted.path) {
                        GetStartedRoute(navController)
                    }
                    composable(Route.Connections.path, deepLinks = listOf(NavDeepLink("wc:/{topic}@2"))) {
                        ConnectionsRoute(navController, connectionsViewModel, showcaseViewModel)
                    }
                    composable("${Route.ConnectionDetails.path}/{connectionId}", arguments = listOf(
                        navArgument("connectionId") { type = NavType.Companion.IntType }
                    )) {
                        ConnectionDetailsRoute(navController, it.arguments?.getInt("connectionId"), connectionsViewModel)
                    }
                    bottomSheet(Route.ScanUri.path) {
                        ScanUriRoute(navController, sheetState, onScanSuccess = { showcaseViewModel.pair(it) })
                    }
                    dialog(Route.SessionProposal.path, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        SessionProposalRoute(navController)
                    }
                    dialog(Route.AuthRequest.path, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        AuthRequestRoute(navController)
                    }
                    dialog(Route.SessionRequest.path, deepLinks = listOf(NavDeepLink("kotlin-showcase:/request")), dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        SessionRequestRoute(navController)
                    }
                    dialog(Route.PasteUri.path, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        PasteUriRoute(onSubmit = {
                            showcaseViewModel.pair(it)
                            navController.popBackStack()
                        })
                    }
                    bottomSheet("${Route.SnackbarMessage.path}/{message}", arguments = listOf(
                        navArgument("message") { type = NavType.Companion.StringType }
                    )) {
                        SnackbarMessageRoute(navController, it.arguments?.getString("message"))
                    }
                }
            }
        )
    }
}

