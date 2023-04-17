package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme

@Composable
fun RoundedMainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startIcon: (@Composable () -> Unit)? = null,
    endIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .background(Web3ModalTheme.colors.mainColor, shape = RoundedCornerShape(18.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        startIcon?.let {
            startIcon()
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = TextStyle(
                color = Web3ModalTheme.colors.onMainColor,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.weight(1f)
        )
        endIcon?.let {
            Spacer(modifier = Modifier.width(6.dp))
            endIcon()
        }
    }
}