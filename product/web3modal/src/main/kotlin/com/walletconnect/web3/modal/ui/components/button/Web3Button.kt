package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun Web3Button(
    state: Web3ModalState,
    accountButtonType: AccountButtonType = AccountButtonType.NORMAL,
    connectButtonSize: ConnectButtonSize = ConnectButtonSize.NORMAL
) {
    val isConnected by state.isConnected.collectAsState(initial = false)

    AnimatedContent(
        targetState = isConnected,
        label = "Web3ButtonState"
    ) { isConnected ->
        if (isConnected) {
            AccountButton(web3ButtonState = state, accountButtonType = accountButtonType)
        } else {
            ConnectButton(web3ButtonState = state, buttonSize = connectButtonSize)
        }
    }
}

private sealed class Web3ButtonState {
    object Loading : Web3ButtonState()
    object Connect : Web3ButtonState()
    object Account : Web3ButtonState()
}
