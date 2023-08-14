package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun LoadingSpinner() {
    CircularProgressIndicator(color = Web3ModalTheme.colors.main100, modifier = Modifier.size(24.dp))
}
