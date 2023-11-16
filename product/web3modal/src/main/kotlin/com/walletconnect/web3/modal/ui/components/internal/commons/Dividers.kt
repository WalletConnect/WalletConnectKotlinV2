package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun FullWidthDivider(modifier: Modifier = Modifier) {
    Divider(color = Web3ModalTheme.colors.grayGlass05, modifier = modifier)
}
