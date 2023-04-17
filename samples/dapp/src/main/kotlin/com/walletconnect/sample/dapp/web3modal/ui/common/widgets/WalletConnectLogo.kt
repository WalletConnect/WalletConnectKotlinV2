package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample_common.ui.theme.PreviewTheme

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
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "WalletConnect",
            style = TextStyle(
                color = Web3ModalTheme.colors.onMainColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Preview
@Composable
private fun PreviewWalletConnectLogo() {
    PreviewTheme {
        WalletConnectLogo()
    }
}