package com.walletconnect.web3.modal.ui.routes.connect.choose_network

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.ui.components.internal.commons.NetworkBottomSection
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.network.ChainNetworkItem
import com.walletconnect.web3.modal.ui.routes.connect.ConnectState
import com.walletconnect.web3.modal.utils.getChainNetworkImageUrl

@Composable
internal fun ChooseNetworkRoute(
    connectState: ConnectState
) {
    val chains = Web3Modal.chains
    val selectedChain by connectState.selectedChain.collectAsState(initial = null)

    ChainNetworkSelector(
        chains = chains,
        selectedChain = selectedChain,
        onChainItemClick = { chain -> connectState.navigateToConnectWallet(chain) },
        onWhatIsWalletClick = { connectState.navigateToHelp() }
    )
}

@Composable
private fun ChainNetworkSelector(
    chains: List<Modal.Model.Chain>,
    selectedChain: Modal.Model.Chain?,
    onChainItemClick: (Modal.Model.Chain) -> Unit,
    onWhatIsWalletClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalSpacer(height = 12.dp)
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
    selectedChain: Modal.Model.Chain?,
    onItemClick: (Modal.Model.Chain) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(horizontal = 10.dp),
        columns = GridCells.Adaptive(76.dp),
        content = {
            itemsIndexed(chains) { _, item ->
                ChainNetworkItem(
                    image = item.chainImage ?: getChainNetworkImageUrl(item.chainReference),
                    isSelected = item.id == selectedChain?.id,
                    isEnabled = true,
                    networkName = item.chainName,
                ) {
                    onItemClick(item)
                }
            }
        }
    )
}
