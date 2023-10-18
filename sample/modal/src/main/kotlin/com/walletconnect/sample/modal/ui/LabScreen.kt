package com.walletconnect.sample.modal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.ui.components.button.AccountButtonType
import com.walletconnect.web3.modal.ui.Web3ModalTheme
import com.walletconnect.web3.modal.ui.components.button.NetworkButton
import com.walletconnect.web3.modal.ui.components.button.Web3Button
import com.walletconnect.web3.modal.ui.components.button.rememberWeb3ModalState

@Composable
fun LabScreen(
    navController: NavController
) {
    val web3ModalState = rememberWeb3ModalState(navController = navController)

    Web3ModalTheme(
        mode = Web3ModalTheme.Mode.AUTO
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Web3Button(state = web3ModalState, accountButtonType = AccountButtonType.MIXED) }
            item { NetworkButton(state = web3ModalState) }
        }
    }
}
