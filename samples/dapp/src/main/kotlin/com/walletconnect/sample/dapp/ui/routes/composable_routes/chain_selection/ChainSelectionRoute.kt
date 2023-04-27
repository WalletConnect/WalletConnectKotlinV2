package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample.dapp.ui.routes.bottom_routes.PairingSelectionResult
import com.walletconnect.sample.dapp.ui.routes.bottom_routes.pairingSelectionResultKey
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.CompletePreviews
import com.walletconnect.sample_common.ui.*
import com.walletconnect.sample_common.ui.commons.BlueButton
import com.walletconnect.sample_common.ui.theme.PreviewTheme
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.ui.navigateToWeb3Modal
import timber.log.Timber

@Composable
fun ChainSelectionRoute(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ChainSelectionViewModel = viewModel()
    val chainsState by viewModel.uiState.collectAsState()
    val selectionResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<PairingSelectionResult>(
            pairingSelectionResultKey,
            PairingSelectionResult.None
        )
        ?.collectAsState()?.value ?: PairingSelectionResult.None

    LaunchedEffect(selectionResult) {
        when (selectionResult) {
            PairingSelectionResult.NewPairing -> {
                viewModel.connectToWallet { uri ->
                    navController.navigateToWeb3Modal(Config.Connect(uri = uri))
                }
            }
            is PairingSelectionResult.SelectedPairing -> viewModel.connectToWallet(selectionResult.position)
            PairingSelectionResult.None -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.walletEvents.collect { event ->
            when(event) {
                DappSampleEvents.SessionApproved -> navController.navigate(Route.Session.path)
                else -> Unit
            }
        }
    }

    ChainSelectionScreen(
        chains = chainsState,
        onChainClick = viewModel::updateChainSelectState,
        onConnectClick = {
            if (viewModel.isAnyChainSelected) {
                if (viewModel.isAnySettledParingExist) {
                    navController.navigate(Route.ParingSelection.path)
                } else {
                    viewModel.connectToWallet { uri ->
                        navController.navigateToWeb3Modal(Config.Connect(uri = uri))
                    }
                }
            } else {
                Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@Composable
private fun ChainSelectionScreen(
    chains: List<ChainSelectionUi>,
    onChainClick: (Int, Boolean) -> Unit,
    onConnectClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WCTopAppBar(titleText = "Chain selection")
        ChainsList(
            chains = chains,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            onChainClick,
        )
        BlueButton(
            text = "Connect",
            onClick = onConnectClick,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun ChainsList(
    chains: List<ChainSelectionUi>,
    modifier: Modifier,
    onChainClick: (Int, Boolean) -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(chains) { index, chain ->
            ChainItem(
                index = index,
                chain = chain,
                onChainClick = onChainClick
            )
        }
    }
}

@Composable
private fun ChainItem(
    index: Int,
    chain: ChainSelectionUi,
    onChainClick: (Int, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable {
                onChainClick(index, chain.isSelected)
            }
            .conditionalModifier(chain.isSelected) {
                Modifier.coloredShadow(
                    chain.color.toColor(),
                    borderRadius = 8.dp,
                    blurRadius = 8.dp,
                    spread = 2f
                )
            }
            .border(width = 1.dp, color = chain.color.toColor(), shape = RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = chain.icon),
            contentDescription = "${chain.chainName} icon"
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = chain.chainName)
    }
}

@CompletePreviews
@Composable
private fun ChainSelectionScreenPreview(
    @PreviewParameter(ChainSelectionStateProvider::class) chains: List<ChainSelectionUi>
) {
    PreviewTheme {
        ChainSelectionScreen(
            chains = chains,
            onChainClick = { _, _ -> },
            onConnectClick = {}
        )
    }
}

private class ChainSelectionStateProvider : PreviewParameterProvider<List<ChainSelectionUi>> {
    override val values: Sequence<List<ChainSelectionUi>>
        get() = sequenceOf(
            Chains.values().map { it.toChainUiState() }
        )
}
