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
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.ui.components.button.AccountButton
import com.walletconnect.web3.modal.ui.components.button.AccountButtonType
import com.walletconnect.web3.modal.ui.components.button.ConnectButton
import com.walletconnect.web3.modal.ui.components.button.ConnectButtonSize
import com.walletconnect.web3.modal.ui.components.button.NetworkButton
import com.walletconnect.web3.modal.ui.components.button.Web3Button
import com.walletconnect.web3.modal.ui.components.button.rememberWeb3ButtonState

@Composable
fun HomeScreen(navController: NavController) {
    val web3ButtonState = rememberWeb3ButtonState(navController = navController)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConnectButton(web3ButtonState = web3ButtonState, buttonSize = ConnectButtonSize.NORMAL)
        Spacer(modifier = Modifier.height(20.dp))
        ConnectButton(web3ButtonState = web3ButtonState, buttonSize = ConnectButtonSize.SMALL)
        Spacer(modifier = Modifier.height(20.dp))
        NetworkButton(web3ButtonState = web3ButtonState)
        Spacer(modifier = Modifier.height(20.dp))
        AccountButton(web3ButtonState, AccountButtonType.NORMAL)
        Spacer(modifier = Modifier.height(20.dp))
        Web3Button(state = web3ButtonState)
    }
}
