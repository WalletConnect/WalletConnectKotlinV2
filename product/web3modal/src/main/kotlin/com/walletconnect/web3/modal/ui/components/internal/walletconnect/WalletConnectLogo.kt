package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
fun WalletConnectLogo(modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet_connect_logo),
            contentDescription = "WalletConnectLogo"
        )
        HorizontalSpacer(width = 4.dp)
        Text(
            text = "WalletConnect",
            style = TextStyle(
                color = Web3ModalTheme.colors.foreground.color100,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Preview
@Composable
private fun PreviewWalletConnectLogo() {
    ComponentPreview {
        WalletConnectLogo()
    }
}