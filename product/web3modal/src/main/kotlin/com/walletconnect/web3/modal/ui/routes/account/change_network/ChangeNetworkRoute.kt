package com.walletconnect.web3.modal.ui.routes.account.change_network

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.ui.components.internal.commons.NetworkBottomSection
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchInput
import com.walletconnect.web3.modal.ui.components.internal.commons.network.ChainNetworkItem
import com.walletconnect.web3.modal.ui.model.UiStateBuilder
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.ethereumChain
import com.walletconnect.web3.modal.ui.previews.testChains
import com.walletconnect.web3.modal.ui.routes.account.AccountState
import com.walletconnect.web3.modal.utils.getChainNetworkImageUrl

@Composable
internal fun ChangeNetworkRoute(
    accountState: AccountState
) {
    val selectedChain by accountState.selectedChain.collectAsState(initial = Web3Modal.getSelectedChainOrFirst())

    UiStateBuilder(uiStateFlow = accountState.accountState) {
        ChangeNetworkScreen(
            chains = Web3Modal.chains,
            accountData = accountState.accountData,
            selectedChain = selectedChain,
//            onChainItemClick = { accountState.changeActiveChain(it) },
            onChainItemClick = { accountState.navigateToChainSwitchRedirect(it) },
            onWhatIsWalletClick = { accountState.navigateToHelp() }
        )
    }
}

@Composable
private fun ChangeNetworkScreen(
    chains: List<Modal.Model.Chain>,
    accountData: AccountData,
    selectedChain: Modal.Model.Chain,
    onChainItemClick: (Modal.Model.Chain) -> Unit,
    onWhatIsWalletClick: () -> Unit
) {
    var searchInputValue by rememberSaveable() { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchInput(
            searchValue = searchInputValue,
            onSearchValueChange = { searchInputValue = it },
            onClearClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        VerticalSpacer(height = 12.dp)
        ChainNetworkGrid(
            chains = chains.filter { it.chainName.contains(searchInputValue, ignoreCase = true) },
            connectedChains = accountData.chains,
            selectedChain = selectedChain,
            onItemClick = { onChainItemClick(it) }
        )
        NetworkBottomSection(onWhatIsWalletClick)
    }
}

@Composable
private fun ChainNetworkGrid(
    chains: List<Modal.Model.Chain>,
    connectedChains: List<Modal.Model.Chain>,
    selectedChain: Modal.Model.Chain,
    onItemClick: (Modal.Model.Chain) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(horizontal = 10.dp),
        columns = GridCells.Adaptive(80.dp),
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
            chains = testChains
        )
        ChangeNetworkScreen(listOf(), accountData, ethereumChain, {}, {})
    }
}
