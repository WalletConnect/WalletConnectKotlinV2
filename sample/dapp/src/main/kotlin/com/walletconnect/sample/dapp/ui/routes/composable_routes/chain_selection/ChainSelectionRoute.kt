package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.android.utils.isPackageInstalled
import com.walletconnect.sample.common.Chains
import com.walletconnect.sample.common.CompletePreviews
import com.walletconnect.sample.common.ui.WCTopAppBarLegacy
import com.walletconnect.sample.common.ui.coloredShadow
import com.walletconnect.sample.common.ui.commons.BlueButton
import com.walletconnect.sample.common.ui.conditionalModifier
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.common.ui.toColor
import com.walletconnect.sample.dapp.BuildConfig
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample.dapp.ui.routes.bottom_routes.PairingSelectionResult
import com.walletconnect.sample.dapp.ui.routes.bottom_routes.pairingSelectionResultKey
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.ui.openWalletConnectModal
import com.walletconnect.wcmodal.ui.state.rememberModalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ChainSelectionRoute(navController: NavController) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()
    val viewModel: ChainSelectionViewModel = viewModel()
    val chainsState by viewModel.uiState.collectAsState()
    rememberModalState(navController = navController)
    val awaitingProposalResponse = viewModel.awaitingSharedFlow.collectAsState(false).value

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collectLatest { event ->
            event.savedStateHandle.get<PairingSelectionResult>(pairingSelectionResultKey)?.let {
                navController.currentBackStackEntry?.savedStateHandle?.remove<PairingSelectionResult>(pairingSelectionResultKey)
                when (it) {
                    PairingSelectionResult.NewPairing -> {
                        WalletConnectModal.setSessionParams(viewModel.getSessionParams())
                        navController.openWalletConnectModal()
                    }

                    PairingSelectionResult.None -> Unit
                    is PairingSelectionResult.SelectedPairing -> {
                        viewModel.connectToWallet(it.position,
                            onSuccess = {
                                println("Proposal sent successfully")
                            },
                            onError = { error ->
                                composableScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Error while connecting: $error", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.walletEvents.collect { event ->
            when (event) {
                DappSampleEvents.SessionApproved -> {
                    viewModel.awaitingProposalResponse(false)
                    navController.navigate(Route.Session.path)
                }

                DappSampleEvents.SessionRejected -> {
                    viewModel.awaitingProposalResponse(false)
                    Toast.makeText(context, "Session has been rejected", Toast.LENGTH_SHORT).show()
                }

                DappSampleEvents.ProposalExpired -> {
                    viewModel.awaitingProposalResponse(false)
                    Toast.makeText(context, "Proposal has been expired", Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.coreEvents.collect { event ->
            if (event is DappSampleEvents.PairingExpired) {
                val pairingType = if (event.pairing.isActive) "Active" else "Inactive"
                Toast.makeText(context, "$pairingType pairing has been expired", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ChainSelectionScreen(
        chains = chainsState,
        awaitingState = awaitingProposalResponse,
        isSampleWalletInstalled = context.isSampleWalletInstalled(),
        onChainClick = viewModel::updateChainSelectState,
        onConnectClick = {
            if (viewModel.isAnyChainSelected) {
                if (viewModel.isAnySettledParingExist) {
                    navController.navigate(Route.ParingSelection.path) {
                        popUpTo(Route.ChainSelection.path)
                    }
                } else {
                    WalletConnectModal.setSessionParams(viewModel.getSessionParams())
                    navController.openWalletConnectModal()
                }
            } else {
                Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        if (viewModel.isAnyChainSelected) {
            viewModel.connectToWallet(
                onSuccess = { uri ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = uri.replace("wc:", "wc://").toUri()
                        `package` = when (BuildConfig.BUILD_TYPE) {
                            "debug" -> SAMPLE_WALLET_DEBUG_PACKAGE
                            "internal" -> SAMPLE_WALLET_INTERNAL_PACKAGE
                            else -> SAMPLE_WALLET_RELEASE_PACKAGE
                        }
                    }
                    context.startActivity(intent)
                },
                onError = { error ->
                    composableScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error while connecting: $error", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun ChainSelectionScreen(
    chains: List<ChainSelectionUi>,
    awaitingState: Boolean,
    isSampleWalletInstalled: Boolean,
    onChainClick: (Int, Boolean) -> Unit,
    onConnectClick: () -> Unit,
    onConnectSampleWalletClick: () -> Unit,
) {


    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            WCTopAppBarLegacy(titleText = "Chain selection")
            ChainsList(
                chains = chains,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                onChainClick,
            )
            BlueButton(
                text = "Connect via WalletConnect Modal",
                onClick = onConnectClick,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
            )
            if (isSampleWalletInstalled) {
                WalletConnectSampleButton(onConnectSampleWalletClick)
            }
        }
        if (awaitingState) {
            Loader()
        }
    }
}

@Composable
private fun BoxScope.Loader() {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(34.dp))
            .background(themedColor(Color(0xFF242425).copy(alpha = .95f), Color(0xFFF2F2F7).copy(alpha = .95f)))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            strokeWidth = 8.dp,
            modifier = Modifier
                .size(75.dp), color = Color(0xFFB8F53D)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Awaiting response...",
            maxLines = 1,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                color = themedColor(Color(0xFFb9b3b5), Color(0xFF484648))
            ),
        )
    }
}

@Composable
private fun WalletConnectSampleButton(onClick: () -> Unit) {
    BlueButton(
        text = "Connect via Wallet Sample",
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp)
    )
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
            awaitingState = false,
            isSampleWalletInstalled = false,
            onChainClick = { _, _ -> },
            onConnectClick = {}
        ) {}
    }
}

private class ChainSelectionStateProvider : PreviewParameterProvider<List<ChainSelectionUi>> {
    override val values: Sequence<List<ChainSelectionUi>>
        get() = sequenceOf(
            Chains.values().map { it.toChainUiState() }
        )
}

private const val SAMPLE_WALLET_DEBUG_PACKAGE = "com.walletconnect.sample.wallet.debug"
private const val SAMPLE_WALLET_INTERNAL_PACKAGE = "com.walletconnect.sample.wallet.internal"
private const val SAMPLE_WALLET_RELEASE_PACKAGE = "com.walletconnect.sample.wallet"
private fun Context.isSampleWalletInstalled() =
    (BuildConfig.BUILD_TYPE == "debug" && packageManager.isPackageInstalled(SAMPLE_WALLET_DEBUG_PACKAGE)) ||
            (BuildConfig.BUILD_TYPE == "release" && packageManager.isPackageInstalled(SAMPLE_WALLET_RELEASE_PACKAGE)) ||
            (BuildConfig.BUILD_TYPE == "internal" && packageManager.isPackageInstalled(SAMPLE_WALLET_INTERNAL_PACKAGE))