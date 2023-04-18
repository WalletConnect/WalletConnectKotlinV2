package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample_common.R
import com.walletconnect.sample_common.ui.theme.PreviewTheme

@Composable
fun Web3ModalTopBar(
    title: String,
    endIcon: (@Composable () -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = Web3ModalTheme.colors.textColor,
                fontSize = 20.sp
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        onBackPressed?.let { onBackClick ->
            Icon(
                tint = Color(0xFF3496ff),
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_left),
                contentDescription = "BackArrow",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() }
            )
        }
        endIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                endIcon()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewWeb3TopBar() {
    PreviewTheme {
        Web3ModalTopBar(title = "Connect your wallet", {})
        Web3ModalTopBar(title = "Scan the code", {})
    }
}