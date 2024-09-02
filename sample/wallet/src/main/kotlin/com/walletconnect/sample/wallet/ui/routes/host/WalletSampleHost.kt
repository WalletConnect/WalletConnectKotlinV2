@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui.routes.host

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.pandulapeter.beagle.DebugMenuView
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.Web3WalletNavGraph
import com.walletconnect.sample.wallet.ui.Web3WalletViewModel
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.PairingEvent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun WalletSampleHost(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    web3walletViewModel: Web3WalletViewModel,
    connectionsViewModel: ConnectionsViewModel,
    getStartedVisited: Boolean,
) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val connectionState by web3walletViewModel.connectionState.collectAsState()
    val bottomBarState = rememberBottomBarMutableState()
    val currentRoute = navController.currentBackStackEntryAsState()
    val isLoader by web3walletViewModel.isLoadingFlow.collectAsState(false)
    val isRequestLoader by web3walletViewModel.isRequestLoadingFlow.collectAsState(false)

    LaunchedEffect(Unit) {
        web3walletViewModel.eventsSharedFlow.collect {
            when (it) {
                is PairingEvent.Error -> {
                    if (navController.currentDestination?.route != Route.Connections.path) {
                        navController.popBackStack(route = Route.Connections.path, inclusive = false)
                    }
                    Toast.makeText(navController.context, it.message, Toast.LENGTH_SHORT).show()
                }

                is PairingEvent.ProposalExpired -> {
                    Toast.makeText(navController.context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = true,
        drawerContent = { BeagleDrawer() },
        bottomBar = { if (currentRoute.value?.destination?.route != Route.GetStarted.path) BottomBar(navController, bottomBarState.value) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Web3WalletNavGraph(
                bottomSheetNavigator = bottomSheetNavigator,
                navController = navController,
                getStartedVisited = getStartedVisited,
                web3walletViewModel = web3walletViewModel,
                connectionsViewModel = connectionsViewModel,
            )

            if (connectionState is ConnectionState.Error) {
                ErrorBanner((connectionState as ConnectionState.Error).message)
            } else if (connectionState is ConnectionState.Ok) {
                RestoredConnectionBanner()
            }

            if (isLoader) {
                Loader(initMessage = "WalletConnect is pairing...", updateMessage = "Pairing is taking longer than usual, please try again...")
            }

            if (isRequestLoader) {
                Loader(initMessage = "Awaiting a request...", updateMessage = "It is taking longer than usual..")
            }

            Timer(web3walletViewModel)
        }
    }
}

@Composable
private fun BoxScope.Timer(web3walletViewModel: Web3WalletViewModel) {
    val timer by web3walletViewModel.timerFlow.collectAsState()
    Text(
        modifier = Modifier
            .align(Alignment.BottomStart),
        text = timer,
        maxLines = 1,
        style = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = themedColor(Color(0xFFb9b3b5), Color(0xFF484648))
        ),
    )
}

@Composable
private fun BeagleDrawer() {
    AndroidView(factory = { DebugMenuView(it) }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun BoxScope.Loader(initMessage: String, updateMessage: String) {
    var shouldChangeText by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        delay(15000)
        shouldChangeText = true
    }

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(34.dp))
            .background(themedColor(Color(0xFF242425).copy(alpha = .95f), Color(0xFFF2F2F7).copy(alpha = .95f)))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            strokeWidth = 8.dp,
            modifier = Modifier
                .size(75.dp), color = Color(0xFFB8F53D)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            textAlign = TextAlign.Center,
            text = if (shouldChangeText) updateMessage else initMessage,
            maxLines = 2,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                color = themedColor(Color(0xFFb9b3b5), Color(0xFF484648))
            ),
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    var shouldShow by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
        delay(5000)
        shouldShow = false
    }

    if (shouldShow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFDC143C))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.invalid_domain),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(color = Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Network connection lost: $message", color = Color.White)
        }
    }
}

@Composable
private fun RestoredConnectionBanner() {
    var shouldShow by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
        delay(2000)
        shouldShow = false
    }
    if (shouldShow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF93c47d))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_check_white),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(color = Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Network connection is OK", color = Color.White)
        }
    }
}

@Preview
@Composable
private fun PreviewPairingLoader() {
    Box() {
        Loader("", "")
    }
}
