package com.walletconnect.sample.dapp.web3modal.ui.routes.scan_code

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.WalletConnectQRCode
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.Web3ModalTopBar
import com.walletconnect.sample.dapp.web3modal.ui.previews.Web3ModalPreview
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample_common.ui.theme.PreviewTheme

@Composable
fun ScanQRCodeRoute(
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
                    colorFilter = ColorFilter.tint(Web3ModalTheme.colors.mainColor),
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
            primaryColor = Web3ModalTheme.colors.onBackgroundColor,
            logoColor = Web3ModalTheme.colors.mainColor
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