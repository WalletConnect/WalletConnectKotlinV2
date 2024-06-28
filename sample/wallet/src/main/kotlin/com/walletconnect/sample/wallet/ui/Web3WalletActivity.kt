@file:OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class
)

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
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.net.URLEncoder

class Web3WalletActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController
    private val web3walletViewModel = Web3WalletViewModel()
    private val connectionsViewModel = ConnectionsViewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent(web3walletViewModel, connectionsViewModel)
        handleWeb3WalletEvents(web3walletViewModel, connectionsViewModel)
        handleCoreEvents(connectionsViewModel)
        askNotificationPermission()
        handleErrors()

        handleAppLink(intent)
    }

    private fun setContent(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel,
    ) {
        setContent {
            val sheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberAnimatedNavController(bottomSheetNavigator)
            this.navController = navController
            println("kobe: set nav controller")
            val sharedPref = getPreferences(MODE_PRIVATE)
            val getStartedVisited = sharedPref.getBoolean("get_started_visited", false)
            WCSampleAppTheme {
                WalletSampleHost(
                    bottomSheetNavigator,
                    navController,
                    web3walletViewModel,
                    connectionsViewModel,
                    getStartedVisited
                )
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
            .onEach {
                if (it.arrayOfArgs.isNotEmpty()) {
                    web3walletViewModel.showRequestLoader(false)
                    navigateWhenReady{
                        navController.navigate(Route.SessionRequest.path)
                    }
                }
            }
            .launchIn(lifecycleScope)

        web3walletViewModel.walletEvents
            .onEach { event ->
                when (event) {
                    is SignEvent.SessionProposal -> navigateWhenReady { navController.navigate(Route.SessionProposal.path) }
                    is SignEvent.SessionAuthenticate ->  navigateWhenReady { navController.navigate(Route.SessionAuthenticate.path) }
                    is SignEvent.ExpiredRequest -> {
                        navController.popBackStack(route = Route.Connections.path, inclusive = false)
                        Toast.makeText(baseContext, "Request expired", Toast.LENGTH_SHORT).show()
                    }

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

    private suspend fun navigateWhenReady(navigate :() -> Unit) {
        if (!::navController.isInitialized) {
            delay(200)
            navigate()
        } else {
            navigate()
        }
    }

    private fun handleAppLink(intent: Intent?) {
        if (intent?.dataString?.contains("wc_ev") == true) {
            Web3Wallet.dispatchEnvelope(intent.dataString ?: "") {
                println("Dispatch error: $it")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAppLink(intent)

        when {
            intent?.dataString?.startsWith("kotlin-web3wallet:/wc") == true -> {
                val uri = intent.dataString?.replace("kotlin-web3wallet:/wc", "kotlin-web3wallet://wc")
                intent.setData(uri?.toUri())
            }

            intent?.dataString?.startsWith("wc:") == true -> {
                val uri = "kotlin-web3wallet://wc?uri=" + URLEncoder.encode(intent.dataString, "UTF-8")
                intent.setData(uri.toUri())
            }
        }

        if (intent?.dataString?.startsWith("kotlin-web3wallet://request") == true) {
            web3walletViewModel.showRequestLoader(true)
        }

        if (intent?.dataString?.startsWith("kotlin-web3wallet://request") == false
            && intent.dataString?.contains("requestId") == false
        ) {
            navController.handleDeepLink(intent)
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
