@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.dapp.ui.routes.host

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample.dapp.ui.DappSampleNavGraph
import com.walletconnect.sample.dapp.ui.routes.Route

@Composable
fun DappSampleHost() {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    var isOfflineState by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetNavigator = BottomSheetNavigator(sheetState)
    val navController = rememberNavController(bottomSheetNavigator)
    val viewModel: DappSampleViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                DappSampleEvents.Disconnect -> navController.navigate(Route.ChainSelection.path)
                is DappSampleEvents.RequestError -> scaffoldState.snackbarHostState.showSnackbar(event.exceptionMsg)
                DappSampleEvents.SessionExtend -> scaffoldState.snackbarHostState.showSnackbar("Session extended")
                is DappSampleEvents.ConnectionEvent -> { isOfflineState = !event.isAvailable }
                else -> Unit
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (isOfflineState) {
                ConnectionIndicator()
            }
            DappSampleNavGraph(
                bottomSheetNavigator = bottomSheetNavigator,
                navController = navController,
                startDestination = Route.ChainSelection.path
            )

        }
    }
}

@Composable
private fun ConnectionIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3496ff))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = "No internet connection", color = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_offline),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = Color.White)
        )
    }
}
