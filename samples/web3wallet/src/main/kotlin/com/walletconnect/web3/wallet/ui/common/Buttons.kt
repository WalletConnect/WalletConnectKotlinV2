package com.walletconnect.web3.wallet.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.wallet.ui.common.generated.AllowButton
import com.walletconnect.web3.wallet.ui.common.generated.DeclineButton


@Composable
fun Buttons(modifier: Modifier = Modifier, onDecline: () -> Unit = {}, onAllow: () -> Unit = {}) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.width(20.dp))
        DeclineButton(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clickable { onDecline() }
        )
        Spacer(modifier = Modifier.width(20.dp))
        AllowButton(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clickable { onAllow() }
        )
        Spacer(modifier = Modifier.width(20.dp))

    }
}