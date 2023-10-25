package com.walletconnect.wcmodal.ui.routes.scan_code

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.modal.ui.components.common.ClickableImage
import com.walletconnect.modal.ui.components.qr.QrCodeType
import com.walletconnect.modal.ui.components.qr.WalletConnectQRCode
import com.walletconnect.modal.utils.isLandscape
import com.walletconnect.wcmodal.R
import com.walletconnect.wcmodal.ui.components.ModalTopBar
import com.walletconnect.wcmodal.ui.preview.ModalPreview
import com.walletconnect.wcmodal.ui.theme.ModalTheme

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
    val qrCodeModifier = if (isLandscape) Modifier else Modifier.padding(horizontal = 20.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModalTopBar(
            title = "Scan the code",
            onBackPressed = onBackArrowClick,
            endIcon = {
                ClickableImage(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy),
                    tint = ModalTheme.colors.main,
                    contentDescription = "Scan Icon",
                    onClick = {
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        clipboardManager.setText(AnnotatedString(uri))
                    }
                )
            }
        )
        WalletConnectQRCode(
            qrData = uri,
            primaryColor = ModalTheme.colors.onBackgroundColor,
            logoColor = ModalTheme.colors.main,
            type = QrCodeType.WCM,
            modifier = qrCodeModifier
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview
@Composable
private fun ScanQRCodePreview() {
    ModalPreview {
        ScanQRCodeContent("47442c19ea7c6a7a836fa3e53af1ddd375438daaeea9acdbf595e989a731b73249a10a7cc0e343ca627e536609",{})
    }
}