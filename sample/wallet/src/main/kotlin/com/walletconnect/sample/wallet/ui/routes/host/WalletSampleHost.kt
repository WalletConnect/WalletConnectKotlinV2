@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui.routes.host

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.pandulapeter.beagle.DebugMenuView
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.Web3WalletNavGraph
import com.walletconnect.sample.wallet.ui.Web3WalletViewModel
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.PairingState

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun WalletSampleHost(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    web3walletViewModel: Web3WalletViewModel,
    connectionsViewModel: ConnectionsViewModel,
    getStartedVisited: Boolean
) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val connectionState = web3walletViewModel.connectionState.collectAsState(ConnectionState.Idle).value
    val pairingState = web3walletViewModel.pairingStateSharedFlow.collectAsState(PairingState.Idle).value

    Scaffold(
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = true,
        drawerContent = { BeagleDrawer() }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Web3WalletNavGraph(
                bottomSheetNavigator = bottomSheetNavigator,
                navController = navController,
                getStartedVisited = getStartedVisited,
                web3walletViewModel = web3walletViewModel,
                connectionsViewModel = connectionsViewModel
            )

            if (connectionState is ConnectionState.Error) {
                ErrorBanner(connectionState.message)
            }

            if (pairingState is PairingState.Error) {
                navController.showSnackbar(pairingState.message)
            }

            if (pairingState is PairingState.Loading) {
                PairingLoader()
            }
        }
    }
}

@Composable
private fun BeagleDrawer() {
    AndroidView(factory = { DebugMenuView(it) }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun BoxScope.PairingLoader() {
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
            text = "WalletConnect is pairing...",
            maxLines = 1,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDC143C))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.orange_warning),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = Color.White)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = message, color = Color.White)
    }
}

@Preview
@Composable
private fun PreviewPairingLoader() {
    Box() {
        PairingLoader()
    }
}
