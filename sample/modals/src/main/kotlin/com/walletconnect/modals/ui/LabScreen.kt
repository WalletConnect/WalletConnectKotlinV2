package com.walletconnect.modals.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.ui.components.button.NetworkButton
import com.walletconnect.web3.modal.ui.components.button.Web3Button
import com.walletconnect.web3.modal.ui.components.button.rememberWeb3ModalState

@Composable
fun LabScreen(
    navController: NavController
) {
    val web3ModalState = rememberWeb3ModalState(navController = navController)
    val isConnected by web3ModalState.isConnected.collectAsState(initial = false)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Web3Button(state = web3ModalState)
        Spacer(modifier = Modifier.height(20.dp))
        NetworkButton(state = web3ModalState)

        if (isConnected) {
            SignButton()
        }
    }
}

@Composable
fun SignButton() {
    Button(onClick = { requestSignMethod() }) {
        Text(text = "Personal sign")
    }
}

private fun requestSignMethod() {
    // todo create sign method
}
