package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
    Divider(color = Web3ModalTheme.colors.grayGlass05, modifier = modifier)
}

@Composable
internal fun FullWidthOrDivider() {
    Row(horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        Divider(modifier = Modifier.weight(1f).padding(end = 10.dp), color = Web3ModalTheme.colors.grayGlass05)
        Text(text = "or")
        Divider(modifier = Modifier.weight(1f).padding(start = 10.dp), color = Web3ModalTheme.colors.grayGlass05)
    }
}
