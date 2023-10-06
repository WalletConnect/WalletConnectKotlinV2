package com.walletconnect.sample.wallet.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.scan_uri.ScanUriRoute
import com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription.UpdateSubscriptionRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connection_details.ConnectionDetailsRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.routes.composable_routes.explorer_dapps.ExploreDappsScreenRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.get_started.GetStartedRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.InboxScreenRoute
import com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications.NotificationsScreenRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.auth_request.AuthRequestRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.paste_uri.PasteUriRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal.SessionProposalRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_request.SessionRequestRoute
import com.walletconnect.sample.wallet.ui.routes.dialog_routes.snackbar_message.SnackbarMessageRoute

@ExperimentalMaterialNavigationApi
@Composable
fun Web3WalletNavGraph(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    web3walletViewModel: Web3WalletViewModel,
    connectionsViewModel: ConnectionsViewModel,
    getStartedVisited: Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = if (getStartedVisited) Route.Connections.path else Route.GetStarted.path,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = Color.Transparent, sheetElevation = 0.dp,
        scrimColor = Color.Unspecified
    ) {
        val sheetState = remember { bottomSheetNavigator.navigatorSheetState }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                    animationSpec = tween(700)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                    animationSpec = tween(700)
                )
            }

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
            composable("${Route.Notifications.path}/{topic}/{name}/{url}", arguments = listOf(
                navArgument("topic") {
                    type = NavType.Companion.StringType
                    nullable = false
                },
                navArgument("name") {
                    type = NavType.Companion.StringType
                    nullable = false
                },
                navArgument("url") {
                    type = NavType.Companion.StringType
                    nullable = false
                }
            )) {
                NotificationsScreenRoute(navController, it.arguments?.getString("topic")!!, it.arguments?.getString("name")!!, it.arguments?.getString("url")!!)
            }
            composable(Route.ExploreDapps.path) {
                ExploreDappsScreenRoute(navController)
            }
            composable(Route.Inbox.path) {
                InboxScreenRoute(navController)
            }
            bottomSheet(Route.ScanUri.path) {
                ScanUriRoute(navController, sheetState, onScanSuccess = { web3walletViewModel.pair(it) })
            }
            bottomSheet("${Route.UpdateSubscription.path}/{topic}", arguments = listOf(
                navArgument("topic") {
                    type = NavType.Companion.StringType
                    nullable = false
                })) {
                UpdateSubscriptionRoute(navController, sheetState, it.arguments?.getString("topic")!!)
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
}

