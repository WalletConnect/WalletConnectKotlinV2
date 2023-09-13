package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.model.Identity
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchInput
import com.walletconnect.web3.modal.ui.components.internal.commons.network.ChainNetworkItem
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.getChainNetworkImageUrl

@Composable
internal fun ChangeNetworkRoute(
    accountData: AccountData,
    changeChain: (AccountData, Chain) -> Unit
) {
    ChangeNetworkScreen(
        chains = Web3Modal.chains,
        accountData = accountData,
        onChainItemClick = changeChain
    )
}

@Composable
private fun ChangeNetworkScreen(
    chains: List<Modal.Model.Chain>,
    accountData: AccountData,
    onChainItemClick: (AccountData, Chain) -> Unit
) {
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
        ChainNetworkGrid(
            chains = chains,
            connectedChains = accountData.chains.filter { it.name.contains(searchInputValue, ignoreCase = true) },
            selectedChain = accountData.selectedChain,
            onItemClick = { onChainItemClick(accountData, it) }
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
private fun ChainNetworkGrid(
    chains: List<Modal.Model.Chain>,
    connectedChains: List<Chain>,
    selectedChain: Chain,
    onItemClick: (Chain) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(horizontal = 10.dp),
        columns = GridCells.Adaptive(76.dp),
        content = {
            itemsIndexed(chains) { _, item ->
                ChainNetworkItem(
                    isSelected = item.id == selectedChain.id,
                    isEnabled = connectedChains.any { it.id == item.id },
                    networkName = item.chainName,
                    image = item.chainImage ?: getChainNetworkImageUrl(item.chainReference)
                ) {
                    onItemClick(connectedChains.find { it.id == item.id }!!)
                }
            }
        }
    )
}

@Composable
@UiModePreview
private fun ChangeNetworkPreview() {
    Web3ModalPreview("Change Network") {
        val accountData = AccountData(
            topic = "",
            address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
            balance = "543 ETH",
            selectedChain = Chain("eip155:1"),
            chains = listOf(Chain("eip155:1")),
            identity = Identity()

        )
        ChangeNetworkScreen(listOf(),  accountData, { _, _ -> })
    }
}