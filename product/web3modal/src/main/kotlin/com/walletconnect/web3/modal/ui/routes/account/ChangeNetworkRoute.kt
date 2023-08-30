package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchInput
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ChangeNetworkRoute() {
    ChangeNetworkScreen()
}

@Composable
private fun ChangeNetworkScreen() {
    var searchInputValue by rememberSaveable() { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        SearchInput(
            searchValue = searchInputValue,
            onSearchValueChange = { searchInputValue = it },
            onClearClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        VerticalSpacer(height = 12.dp)
        NetworkChainsGrid(
            modifier = Modifier.weight(1f)
        )
        FullWidthDivider()
        VerticalSpacer(height = 12.dp)
        Text(
            text = "Your connected wallet may not support some of the networks available for this dApp",
            style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color300),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun NetworkChainsGrid(
    modifier: Modifier = Modifier
) {
    // TODO finish after creating data models, next PR
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        content = {}
    )
}

@Composable
@UiModePreview
private fun ChangeNetworkPreview() {
    Web3ModalPreview("Change Network") {
        ChangeNetworkScreen()
    }
}