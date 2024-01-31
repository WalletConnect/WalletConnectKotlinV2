package com.walletconnect.web3.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.email.input.EmailInput
import com.walletconnect.web3.modal.ui.components.internal.email.input.rememberEmailInputState

@Composable
fun Web3ModalTestScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
    ) {
        val emailState = rememberEmailInputState()

        EmailInput(emailInputState = emailState)
    }

}