package com.walletconnect.modal.ui.components.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.modal.ui.ComponentPreview
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.modalcore.R

@Composable
fun WalletConnectLogo(modifier: Modifier = Modifier, color: Color) {
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
                color = color,
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
        WalletConnectLogo(color = Color.Blue)
    }
}