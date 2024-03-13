@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)

package com.walletconnect.sample.wallet.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.common.ui.theme.WCSampleAppTheme
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.routes.host.WalletSampleHost
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class Web3WalletActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val web3walletViewModel = Web3WalletViewModel()
        val connectionsViewModel = ConnectionsViewModel()
        setContent(web3walletViewModel, connectionsViewModel)
        handleWeb3WalletEvents(web3walletViewModel, connectionsViewModel)
        handleCoreEvents(connectionsViewModel)
        askNotificationPermission()
        handleErrors()
    }

    private fun setContent(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel,
    ) {
        setContent {
            val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberAnimatedNavController(bottomSheetNavigator)
            this.navController = navController
            val sharedPref = getPreferences(MODE_PRIVATE)
            val getStartedVisited = sharedPref.getBoolean("get_started_visited", false)
            WCSampleAppTheme() {
                WalletSampleHost(bottomSheetNavigator, navController, web3walletViewModel, connectionsViewModel, getStartedVisited)
            }
        }
    }

    private fun handleCoreEvents(connectionsViewModel: ConnectionsViewModel) {
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
    }


    private fun handleErrors() {
        NotifyDelegate.notifyErrors
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { error -> Timber.e(error.throwable) }
            .launchIn(lifecycleScope)
    }

    private fun handleWeb3WalletEvents(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel,
    ) {
        web3walletViewModel.sessionRequestStateFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it.arrayOfArgs.isNotEmpty()) {
                    navController.navigate(Route.SessionRequest.path)
                }
            }
            .launchIn(lifecycleScope)

        web3walletViewModel.walletEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is SignEvent.SessionProposal -> navController.navigate(Route.SessionProposal.path)
                    is SignEvent.ExpiredRequest -> {
                        navController.popBackStack(route = Route.Connections.path, inclusive = false)
                        Toast.makeText(baseContext, "Request expired", Toast.LENGTH_SHORT).show()
                    }

                    is SignEvent.Disconnect -> {
                        connectionsViewModel.refreshConnections()
                        navController.navigate(Route.Connections.path)
                    }

                    is AuthEvent.OnRequest -> navController.navigate(Route.AuthRequest.path)
                    is SignEvent.SessionAuthenticate -> navController.navigate(Route.SessionAuthenticate.path)

                    else -> Unit
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.dataString?.contains("wc:") == true) {
            val uri = intent.dataString?.replace("wc:", "wc://")
            intent.setData(uri?.toUri())
        }
        navController.handleDeepLink(intent)
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
