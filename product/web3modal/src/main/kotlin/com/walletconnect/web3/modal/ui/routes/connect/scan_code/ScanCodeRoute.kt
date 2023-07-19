package com.walletconnect.web3.modal.ui.routes.connect.scan_code

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectQRCode
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ScanQRCodeRoute(
    navController: NavController,
    uri: String
) {
    ScanQRCodeContent(
        uri = uri,
        onBackArrowClick = navController::popBackStack
    )
}

@Composable
private fun ScanQRCodeContent(
    uri: String,
    onBackArrowClick: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Column {
        Web3ModalTopBar(
            title = "Scan the code",
            onBackPressed = onBackArrowClick,
            endIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy),
                    colorFilter = ColorFilter.tint(Web3ModalTheme.colors.main100),
                    contentDescription = "Scan Icon",
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        clipboardManager.setText(AnnotatedString(uri))
                    }
                )
            }
        )
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
        ScanQRCodeContent("47442c19ea7c6a7a836fa3e53af1ddd375438daaeea9acdbf595e989a731b73249a10a7cc0e343ca627e536609",{})
    }
}