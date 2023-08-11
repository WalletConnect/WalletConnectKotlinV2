package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun FullWidthDivider(modifier: Modifier = Modifier) {
    Divider(color = Web3ModalTheme.colors.overlay05, modifier = modifier)
}

@Composable
internal fun DividerOr() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FullWidthDivider(modifier = Modifier.weight(1f))
        Text(
            text = "or",
            style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color200),
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        FullWidthDivider(modifier = Modifier.weight(1f))
    }
}