package com.walletconnect.modals.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.ui.components.button.AccountButton
import com.walletconnect.web3.modal.ui.components.button.AccountButtonType
import com.walletconnect.web3.modal.ui.components.button.ConnectButton
import com.walletconnect.web3.modal.ui.components.button.ConnectButtonSize
import com.walletconnect.web3.modal.ui.components.button.Web3Button
import com.walletconnect.web3.modal.ui.components.button.rememberWeb3ModalState

@Composable
fun HomeScreen(navController: NavController) {
    val web3ModalState = rememberWeb3ModalState(navController = navController)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConnectButton(state = web3ModalState, buttonSize = ConnectButtonSize.NORMAL)
        Spacer(modifier = Modifier.height(20.dp))
        ConnectButton(state = web3ModalState, buttonSize = ConnectButtonSize.SMALL)
        Spacer(modifier = Modifier.height(20.dp))
        AccountButton(web3ModalState, AccountButtonType.NORMAL)
        Spacer(modifier = Modifier.height(20.dp))
        AccountButton(web3ModalState, AccountButtonType.MIXED)
        Spacer(modifier = Modifier.height(20.dp))
        Web3Button(state = web3ModalState)
    }
}
