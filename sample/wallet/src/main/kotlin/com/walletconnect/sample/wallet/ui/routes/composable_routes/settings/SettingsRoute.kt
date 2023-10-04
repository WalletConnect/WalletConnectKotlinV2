package com.walletconnect.sample.wallet.ui.routes.composable_routes.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.walletconnect.sample.common.ui.WCTopAppBar
import com.walletconnect.sample.wallet.BuildConfig

@Composable
fun SettingsRoute(navController: NavHostController) {
    SettingsScreen(navController) { navController.popBackStack() }
}


@Composable
private fun SettingsScreen(
    navController: NavHostController,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        WCTopAppBar(titleText = "Settings")
        Text(BuildConfig.VERSION_NAME)
    }
}