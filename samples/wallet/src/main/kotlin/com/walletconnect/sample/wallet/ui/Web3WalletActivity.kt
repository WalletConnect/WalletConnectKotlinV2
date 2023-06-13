@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample_common.ui.theme.WCSampleAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import kotlin.random.Random
import kotlin.random.nextUInt

class Web3WalletActivity : ComponentActivity() {
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
        handlePushEvents(web3walletViewModel)
        handleCoreEvents(connectionsViewModel)
        askNotificationPermission()
        createNotificationChannel()
        setContent(web3walletViewModel, connectionsViewModel)
    }

    private fun setContent(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel
    ) {
        setContent {
            val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberNavController(bottomSheetNavigator)
            this.navController = navController

            val sharedPref = getPreferences(MODE_PRIVATE)
            val getStartedVisited = sharedPref.getBoolean("get_started_visited", false)
            WCSampleAppTheme() {
                Web3WalletNavGraph(
                    bottomSheetNavigator = bottomSheetNavigator, navController = navController,
                    getStartedVisited = getStartedVisited, web3walletViewModel = web3walletViewModel, connectionsViewModel = connectionsViewModel
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

    private fun handlePushEvents(web3walletViewModel: Web3WalletViewModel) {
        web3walletViewModel.pushEvents
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is PushRequest -> {
                        val peerName = URLEncoder.encode(event.peerName, Charsets.UTF_8.name())
                        val peerDesc = URLEncoder.encode(event.peerDesc, Charsets.UTF_8.name())
                        val iconUrl = event.icon?.run { URLEncoder.encode(this, Charsets.UTF_8.name()) }
                        val redirectUrl = event.redirect?.run { URLEncoder.encode(this, Charsets.UTF_8.name()) }
                        val route = StringBuilder(Route.PushRequest.path)
                            .append("?")
                            .append("${Route.PushRequest.KEY_REQUEST_ID}=${event.requestId}")
                            .append("&")
                            .append("${Route.PushRequest.KEY_PEER_NAME}=$peerName")
                            .append("&")
                            .append("${Route.PushRequest.KEY_PEER_DESC}=$peerDesc")
                            .append("&")
                            .append("${Route.PushRequest.KEY_ICON_URL}=$iconUrl")
                            .append("&")
                            .append("${Route.PushRequest.KEY_REDIRECT}=$redirectUrl")
                            .toString()
                        withContext(Dispatchers.Main) {
                            navController.navigate(route)
                        }
                    }
                    is PushMessage -> {
                        val notificationBuilder = NotificationCompat.Builder(this, "Push")
                            .setSmallIcon(com.walletconnect.sample_common.R.drawable.ic_walletconnect_circle_blue)
                            .setContentText(event.title)
                            .setContentText(event.body)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return@onEach
                        }
                        NotificationManagerCompat.from(this).notify(Random.nextUInt().toInt(), notificationBuilder.build())
                    }
                    else -> Unit
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(lifecycleScope)
    }

    private fun handleWeb3WalletEvents(
        web3walletViewModel: Web3WalletViewModel,
        connectionsViewModel: ConnectionsViewModel
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
            val name = "PushSample"
            val descriptionText = "Sample for Push SDK"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Push", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
