package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.AutoScrollingWalletList
import com.walletconnect.web3.modal.ui.components.internal.commons.MainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.RoundedOutLineButton
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.ConnectYourWalletStateProvider
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.toDeeplinkUri

@Composable
internal fun ConnectYourWalletRoute(
    navController: NavController,
    uri: String,
) {
    val context = LocalContext.current
    val viewModel: ConnectYourWalletViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getWalletRecommendations()
    }

    ConnectYourWalletContent(uiState = uiState,
        onSelectWallet = { context.startActivity(Intent(Intent.ACTION_VIEW, uri.toDeeplinkUri())) },
        onScanIconClick = { navController.navigate(Route.ScanQRCode.path) },
        onGetAWalletClick = { navController.navigate(Route.GetAWallet.path) })
}

@Composable
private fun ConnectYourWalletContent(
    uiState: ConnectYourWalletUI,
    onSelectWallet: () -> Unit,
    onScanIconClick: () -> Unit,
    onGetAWalletClick: () -> Unit,
) {
    Column {
        Web3ModalTopBar(title = "Connect your wallet", endIcon = {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
                colorFilter = ColorFilter.tint(Web3ModalTheme.colors.main),
                contentDescription = "Scan Icon",
                modifier = Modifier.clickable { onScanIconClick() })
        })
        SelectWalletState(
            uiState = uiState,
            onSelectWallet = onSelectWallet
        )
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Web3ModalTheme.colors.dividerColor)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Web3ModalTheme.colors.secondaryBackgroundColor)
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose WalletConnect to see supported apps on your device",
                style = TextStyle(
                    color = Web3ModalTheme.colors.secondaryTextColor, textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            RoundedOutLineButton("I don't have a wallet", onGetAWalletClick, endIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_external_link),
                    colorFilter = ColorFilter.tint(Web3ModalTheme.colors.main),
                    contentDescription = null,
                )
            })
        }
    }
}

@Composable
private fun SelectWalletState(
    uiState: ConnectYourWalletUI,
    onSelectWallet: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        uiState.wallets?.let { wallets ->
            if (wallets.isNotEmpty()) {
                AutoScrollingWalletList(uiState.wallets)
            }
        }
        MainButton(
            text = "Select Wallet",
            onClick = { onSelectWallet() },
            modifier = Modifier
                .height(60.dp)
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
private fun ConnectYourWalletPreview(
    @PreviewParameter(ConnectYourWalletStateProvider::class) state: ConnectYourWalletUI
) {
    Web3ModalPreview {
        ConnectYourWalletContent(state, {}, {}, {})
    }
}
