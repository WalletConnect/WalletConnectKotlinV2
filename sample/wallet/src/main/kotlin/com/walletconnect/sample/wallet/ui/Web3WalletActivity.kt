@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.common.ui.theme.WCSampleAppTheme
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.routes.host.WalletSampleHost
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        val web3walletViewModel: Web3WalletViewModel = Web3WalletViewModel()
        val connectionsViewModel: ConnectionsViewModel = ConnectionsViewModel()
        handleWeb3WalletEvents(web3walletViewModel, connectionsViewModel)
        handleCoreEvents(connectionsViewModel)
        askNotificationPermission()
        createNotificationChannel()
        setContent(web3walletViewModel, connectionsViewModel)
    }

    private fun setContent(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel,
    ) {
        setContent {
            val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberNavController(bottomSheetNavigator)
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

    private fun handleWeb3WalletEvents(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel,
    ) {
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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NotifySample"
            val descriptionText = "Sample for Notify SDK"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Notify", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
