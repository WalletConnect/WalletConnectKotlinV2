package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
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
            AccountButton(state = state, accountButtonType = accountButtonType)
        } else {
            ConnectButton(state = state, buttonSize = connectButtonSize)
        }
    }
}
