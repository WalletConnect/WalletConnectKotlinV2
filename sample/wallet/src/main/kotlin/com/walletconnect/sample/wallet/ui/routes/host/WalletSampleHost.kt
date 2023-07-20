@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui.routes.host

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.wallet.ConnectionState
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.connectionStateFlow
import com.walletconnect.sample.wallet.ui.Web3WalletNavGraph
import com.walletconnect.sample.wallet.ui.Web3WalletViewModel
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel

@Composable
fun WalletSampleHost(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    web3walletViewModel: Web3WalletViewModel,
    connectionsViewModel: ConnectionsViewModel,
    getStartedVisited: Boolean
) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val connectionState = connectionStateFlow.collectAsState(ConnectionState.Idle).value

    Scaffold(
        scaffoldState = scaffoldState
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (connectionState is ConnectionState.Error) {
                ErrorBanner(connectionState.message)
            }
            Web3WalletNavGraph(
                bottomSheetNavigator = bottomSheetNavigator, navController = navController,
                getStartedVisited = getStartedVisited, web3walletViewModel = web3walletViewModel, connectionsViewModel = connectionsViewModel
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDC143C))
            .padding(horizontal = 16.dp, vertical = 8.dp),
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