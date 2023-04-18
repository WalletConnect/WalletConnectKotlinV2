package com.walletconnect.sample.dapp.web3modal.ui.routes.connect_wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.web3modal.ui.Route
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.Web3ModalTopBar
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample_common.ui.theme.PreviewTheme

@Composable
fun ConnectYourWalletRoute(
    navController: NavController
) {
    ConnectYourWalletContent(
        onScanIconClick = { navController.navigate(Route.ScanQRCode.path) }
    )
}

@Composable
private fun ConnectYourWalletContent(
    onScanIconClick: () -> Unit,
) {
    Column {
        Web3ModalTopBar(
            title = "Connect your wallet",
            endIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
                    colorFilter = ColorFilter.tint(Web3ModalTheme.colors.mainColor),
                    contentDescription = "Scan Icon",
                    modifier = Modifier.clickable { onScanIconClick() }
                )
            }
        )
    }
}

@Preview
@Composable
private fun ConnectYourWalletPreview() {
    PreviewTheme {
        ConnectYourWalletContent({})
    }
}