package com.walletconnect.modals.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.modals.common.HomeViewModel
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.ui.navigateToWeb3Modal

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            viewModel.connectYourWallet { uri -> navController.navigateToWeb3Modal(Config.Connect(uri)) }
        }) {
            Text(text = "Connect Wallet")
        }
    }
}
