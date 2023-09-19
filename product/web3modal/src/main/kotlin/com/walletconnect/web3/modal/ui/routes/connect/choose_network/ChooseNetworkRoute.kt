package com.walletconnect.web3.modal.ui.routes.connect.choose_network

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.network.ChainNetworkItem
import com.walletconnect.web3.modal.ui.routes.connect.ConnectState
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.getChainNetworkImageUrl

@Composable
internal fun ChooseNetworkRoute(
    connectState: ConnectState
) {
    val chains = Web3Modal.chains
    var selectedChain by remember { mutableStateOf(Web3Modal.selectedChain ?: chains.first()) }

    ChainNetworkSelector(
        chains = chains,
        selectedChain = selectedChain,
        onChainItemClick = { chain -> connectState.navigateToConnectWallet(chain).also { selectedChain = chain } }
    )
}

@Composable
private fun ChainNetworkSelector(
    chains: List<Modal.Model.Chain>,
    selectedChain: Modal.Model.Chain,
    onChainItemClick: (Modal.Model.Chain) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        VerticalSpacer(height = 12.dp)
        ChainNetworkGrid(
            chains = chains,
            selectedChain = selectedChain,
            onItemClick = { onChainItemClick(it) }
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
    selectedChain: Modal.Model.Chain,
    onItemClick: (Modal.Model.Chain) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(horizontal = 10.dp),
        columns = GridCells.Adaptive(80.dp),
        content = {
            itemsIndexed(chains) { _, item ->
                ChainNetworkItem(
                    image = item.chainImage ?: getChainNetworkImageUrl(item.chainReference),
                    isSelected = item.id == selectedChain.id,
                    isEnabled = true,
                    networkName = item.chainName,
                ) {
                    onItemClick(item)
                }
            }
        }
    )
}
