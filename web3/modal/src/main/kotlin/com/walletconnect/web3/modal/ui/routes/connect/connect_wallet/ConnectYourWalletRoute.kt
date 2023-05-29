package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.domain.model.Wallet
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.components.internal.commons.AutoScrollingWalletList
import com.walletconnect.web3.modal.ui.components.internal.commons.MainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.RoundedOutLineButton
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.toDeeplinkUri
import timber.log.Timber

@Composable
internal fun ConnectYourWalletRoute(
    navController: NavController,
    uri: String,
    wallets: List<Wallet>
) {
    val context = LocalContext.current

    ConnectYourWalletContent(
        wallets = wallets,
        onSelectWallet = { selectWallet(context, uri) },
        onScanIconClick = { navController.navigate(Route.ScanQRCode.path) },
        onGetAWalletClick = { navController.navigate(Route.GetAWallet.path) })
}

private fun selectWallet(context: Context, uri: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toDeeplinkUri()))
    } catch (e: Exception) {
        Timber.e(e)
    }
}

@Composable
private fun ConnectYourWalletContent(
    wallets: List<Wallet>,
    onSelectWallet: () -> Unit,
    onScanIconClick: () -> Unit,
    onGetAWalletClick: () -> Unit,
) {
    Column {
        Web3ModalTopBar(title = "Connect your wallet", endIcon = {
            //TODO: for Android 21 test purposes, change
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet_connect_logo),
                colorFilter = ColorFilter.tint(Web3ModalTheme.colors.main),
                contentDescription = "Scan Icon",
                modifier = Modifier.clickable { onScanIconClick() })
        })
        SelectWallet(
            wallets = wallets,
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
private fun SelectWallet(
    wallets: List<Wallet>,
    onSelectWallet: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        if (wallets.isNotEmpty()) {
            AutoScrollingWalletList(wallets)
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
private fun ConnectYourWalletPreview() {
    Web3ModalPreview {
        ConnectYourWalletContent(listOf(), {}, {}, {})
    }
}
