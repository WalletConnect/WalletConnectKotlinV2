package com.walletconnect.sample.dapp.ui.routes.composable_routes.session

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.walletconnect.sample_common.ui.WCTopAppBar

@Composable
fun SessionRoute(
    navController: NavController
) {
    SessionScreen(
        onBackPressed = { navController.popBackStack() },
        onSessionClick = {}
    )
}

@Composable
private fun SessionScreen(
    onBackPressed: () -> Unit,
    onSessionClick: () -> Unit
) {
    Column {
        WCTopAppBar(
            titleText = "Session Chains",
            onBackIconClick = onBackPressed
        )
    }
}