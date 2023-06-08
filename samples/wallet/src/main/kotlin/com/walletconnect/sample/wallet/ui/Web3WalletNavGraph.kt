@file:OptIn(ExperimentalComposeUiApi::class)

package com.walletconnect.sample.wallet.ui

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
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.scan_uri.ScanUriRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connection_details.ConnectionDetailsRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.routes.composable_routes.get_started.GetStartedRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications.NotificationsScreenRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.web3inbox.Web3InboxRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.auth_request.AuthRequestRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.paste_uri.PasteUriRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.push_request.PushRequestRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal.SessionProposalRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_request.SessionRequestRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.snackbar_message.SnackbarMessageRoute
import com.walletconnect.web3.inbox.client.Web3Inbox

@ExperimentalMaterialNavigationApi
@Composable
fun Web3WalletNavGraph(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    web3walletViewModel: Web3WalletViewModel,
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
                val web3InboxState = Web3Inbox.rememberWeb3InboxState()

                NavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    composable(Route.GetStarted.path) {
                        GetStartedRoute(navController)
                    }
                    composable(Route.Connections.path, deepLinks = listOf(NavDeepLink("wc://{topic}@2"))) {
                        ConnectionsRoute(navController, connectionsViewModel, web3walletViewModel)
                    }
                    composable("${Route.ConnectionDetails.path}/{connectionId}", arguments = listOf(
                        navArgument("connectionId") { type = NavType.Companion.IntType }
                    )) {
                        ConnectionDetailsRoute(navController, it.arguments?.getInt("connectionId"), connectionsViewModel)
                    }
                    composable(Route.Web3Inbox.path) {
                        Web3InboxRoute(web3InboxState)
                    }
                    composable(Route.Notifications.path) {
                        NotificationsScreenRoute(navController)
                    }
                    bottomSheet(Route.ScanUri.path) {
                        ScanUriRoute(navController, sheetState, onScanSuccess = { web3walletViewModel.pair(it) })
                    }
                    dialog(Route.SessionProposal.path, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        SessionProposalRoute(navController)
                    }
                    dialog(Route.AuthRequest.path, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        AuthRequestRoute(navController)
                    }
                    dialog(Route.SessionRequest.path, deepLinks = listOf(NavDeepLink("kotlin-web3wallet:/request")), dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        SessionRequestRoute(navController)
                    }
                    dialog("${Route.PushRequest.path}?${Route.PushRequest.KEY_REQUEST_ID}={requestId}&${Route.PushRequest.KEY_PEER_NAME}={peerName}&${Route.PushRequest.KEY_PEER_DESC}={peerDesc}&${Route.PushRequest.KEY_ICON_URL}={iconUrl}&${Route.PushRequest.KEY_REDIRECT}={redirect}",
                        arguments = listOf(
                            navArgument("requestId") { type = NavType.LongType },
                            navArgument("peerName") { type = NavType.StringType },
                            navArgument("peerDesc") { type = NavType.StringType },
                            navArgument("iconUrl") { type = NavType.StringType },
                            navArgument("redirect") { type = NavType.StringType }
                        ),
                        dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) { backStackEntry ->
                        PushRequestRoute(
                            navController,
                            backStackEntry.arguments?.getLong("requestId")!!,
                            backStackEntry.arguments?.getString("peerName")!!,
                            backStackEntry.arguments?.getString("peerDesc")!!,
                            backStackEntry.arguments?.getString("iconUrl"),
                            backStackEntry.arguments?.getString("redirect"),
                        )
                    }
                    dialog("${Route.PushProposal.path}?${Route.PushProposal.KEY_REQUEST_ID}={requestId}&${Route.PushProposal.KEY_PEER_NAME}={peerName}&${Route.PushProposal.KEY_PEER_DESC}={peerDesc}&${Route.PushProposal.KEY_ICON_URL}={iconUrl}&${Route.PushProposal.KEY_REDIRECT}={redirect}",
                        arguments = listOf(
                            navArgument("requestId") { type = NavType.LongType },
                            navArgument("peerName") { type = NavType.StringType },
                            navArgument("peerDesc") { type = NavType.StringType },
                            navArgument("iconUrl") { type = NavType.StringType },
                            navArgument("redirect") {
                                nullable = true
                                type = NavType.StringType
                            }
                        ),
                        dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) { backStackEntry ->
                        PushRequestRoute(
                            navController,
                            backStackEntry.arguments?.getLong("requestId")!!,
                            backStackEntry.arguments?.getString("peerName")!!,
                            backStackEntry.arguments?.getString("peerDesc")!!,
                            backStackEntry.arguments?.getString("iconUrl"),
                            backStackEntry.arguments?.getString("redirect"),
                        )
                    }
                    dialog(Route.PasteUri.path, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {
                        PasteUriRoute(onSubmit = {
                            web3walletViewModel.pair(it)
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

