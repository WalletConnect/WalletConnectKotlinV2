package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.wcmodal.ui.openWalletConnectModal

@Composable
fun AccountScreen(navController: NavController) {
    Box {
        Column(
            verticalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            SignInWithWalletSection(onClick = { navController.openWalletConnectModal() })
        }
    }
}