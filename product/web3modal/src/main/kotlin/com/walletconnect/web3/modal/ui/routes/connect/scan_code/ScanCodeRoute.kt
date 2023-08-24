package com.walletconnect.web3.modal.ui.routes.connect.scan_code

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.qr.WalletConnectQRCode
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.CopyActionEntry
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ScanQRCodeRoute(uri: String) {
    val context: Context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    ScanQRCodeContent(
        uri = uri,
        onCopyLinkClick = {
            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
            clipboardManager.setText(AnnotatedString(uri))
        }
    )
}

@Composable
private fun ScanQRCodeContent(
    uri: String,
    onCopyLinkClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        QRCode(uri = uri)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Scan this QR code with your phone",
            modifier = Modifier.fillMaxWidth(),
            style = Web3ModalTheme.typo.paragraph500,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        CopyActionEntry(modifier = Modifier.fillMaxWidth(), onClick = onCopyLinkClick)
    }
}

@Composable
private fun QRCode(uri: String) {
    if (isSystemInDarkTheme()) {
        Box(
            modifier = Modifier
                .background(Web3ModalTheme.colors.inverse100, shape = RoundedCornerShape(36.dp))
                .padding(16.dp)
        ) {
            WalletConnectQRCode(
                qrData = uri,
                primaryColor = Web3ModalTheme.colors.inverse000,
                logoColor = Web3ModalTheme.colors.main100
            )
        }
    } else {
        WalletConnectQRCode(
            qrData = uri,
            primaryColor = Web3ModalTheme.colors.inverse000,
            logoColor = Web3ModalTheme.colors.main100
        )
    }
}

@UiModePreview
@Composable
private fun ScanQRCodePreview() {
    Web3ModalPreview("Mobile Wallets") {
        ScanQRCodeContent("47442c19ea7c6a7a836fa3e53af1ddd375438daaeea9acdbf595e989a731b73249a10a7cc0e343ca627e536609", {})
    }
}