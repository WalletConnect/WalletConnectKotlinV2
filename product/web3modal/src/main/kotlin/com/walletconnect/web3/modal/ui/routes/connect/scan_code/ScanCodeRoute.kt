package com.walletconnect.web3.modal.ui.routes.connect.scan_code

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.modal.ui.components.qr.WalletConnectQRCode
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ScanQRCodeRoute(
    navController: NavController,
    uri: String
) {
    ScanQRCodeContent(uri = uri)
}

@Composable
private fun ScanQRCodeContent(uri: String) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        WalletConnectQRCode(
            qrData = uri,
            primaryColor = Web3ModalTheme.colors.foreground.color100,
            logoColor = Web3ModalTheme.colors.main100
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview
@Composable
private fun ScanQRCodePreview() {
    Web3ModalPreview {
        ScanQRCodeContent("47442c19ea7c6a7a836fa3e53af1ddd375438daaeea9acdbf595e989a731b73249a10a7cc0e343ca627e536609")
    }
}