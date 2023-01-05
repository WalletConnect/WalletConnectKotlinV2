package com.walletconnect.web3.wallet.ui.common.generated

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.walletconnect.web3.wallet.sample.R

@Composable
fun CloseButton(modifier: Modifier = Modifier) {
    Image(painter = painterResource(R.drawable.close_button), contentDescription = null, modifier = modifier)
}

