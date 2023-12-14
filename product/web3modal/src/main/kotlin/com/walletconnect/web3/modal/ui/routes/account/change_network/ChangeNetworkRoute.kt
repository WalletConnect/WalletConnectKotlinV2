package com.walletconnect.web3.modal.ui.routes.account.change_network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.ui.components.internal.commons.NetworkBottomSection
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.network.ChainNetworkItem
import com.walletconnect.web3.modal.ui.model.UiStateBuilder
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.ethereumChain
import com.walletconnect.web3.modal.ui.previews.testChains
import com.walletconnect.web3.modal.ui.routes.account.AccountViewModel
import com.walletconnect.web3.modal.utils.getChainNetworkImageUrl

@Composable
internal fun ChangeNetworkRoute(
    accountViewModel: AccountViewModel
) {
    val selectedChain by accountViewModel.selectedChain.collectAsState(initial = Web3Modal.getSelectedChainOrFirst())

    UiStateBuilder(uiStateFlow = accountViewModel.accountState) {
        ChangeNetworkScreen(
            chains = Web3Modal.chains,
            selectedChain = selectedChain,
            onChainItemClick = { accountViewModel.changeActiveChain(it) },
            onWhatIsWalletClick = { accountViewModel.navigateToHelp() }
        )
    }
}

@Composable
private fun ChangeNetworkScreen(
    chains: List<Modal.Model.Chain>,
    selectedChain: Modal.Model.Chain,
    onChainItemClick: (Modal.Model.Chain) -> Unit,
    onWhatIsWalletClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalSpacer(height = 8.dp)
        ChainNetworkGrid(
            chains = chains,
            selectedChain = selectedChain,
            onItemClick = { onChainItemClick(it) }
        )
        NetworkBottomSection(onWhatIsWalletClick)
    }
}

@Composable
private fun ChainNetworkGrid(
    chains: List<Modal.Model.Chain>,
    selectedChain: Modal.Model.Chain,
    onItemClick: (Modal.Model.Chain) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.FixedSize(82.dp),
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.Center,
        content = {
            itemsIndexed(chains) { _, item ->
                ChainNetworkItem(
                    isSelected = item.id == selectedChain.id,
                    networkName = item.chainName,
                    image = item.chainImage ?: getChainNetworkImageUrl(item.chainReference)
                ) {
                    onItemClick(item)
                }
            }
        }
    )
}

@Composable
@UiModePreview
private fun ChangeNetworkPreview() {
    Web3ModalPreview("Change Network") {
        ChangeNetworkScreen(testChains, ethereumChain, {}, {})
    }
}
