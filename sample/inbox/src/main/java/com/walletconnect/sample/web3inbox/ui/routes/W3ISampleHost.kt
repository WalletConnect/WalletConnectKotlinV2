@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.web3inbox.ui.routes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.web3inbox.R
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun W3ISampleHost() {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    var isOfflineState by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetNavigator = BottomSheetNavigator(sheetState)
    val navController = rememberNavController(bottomSheetNavigator)
    val viewModel: W3ISampleViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.events.collect { event ->
            Timber.d(event.toString())
            when (event) {
                W3ISampleEvents.Disconnect -> navController.navigate(Route.SelectAccount.path)
                is W3ISampleEvents.RequestError -> scaffoldState.snackbarHostState.showSnackbar(event.exceptionMsg.take(200))
                W3ISampleEvents.SessionExtend -> scaffoldState.snackbarHostState.showSnackbar("Session extended")
                is W3ISampleEvents.ConnectionEvent -> isOfflineState = !event.isAvailable
                is W3ISampleEvents.SessionApproved -> SharedPrefStorage.setLastLoggedInAccount(context, event.account)
                else -> Unit
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (isOfflineState) {
                ConnectionIndicator()
            }
            W3ISampleNavGraph(
                bottomSheetNavigator = bottomSheetNavigator,
                navController = navController,
            )
        }
    }
}


@Composable
private fun ConnectionIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = "No internet connection", color = MaterialTheme.colors.onPrimary)
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_offline),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onPrimary)
        )
    }
}
