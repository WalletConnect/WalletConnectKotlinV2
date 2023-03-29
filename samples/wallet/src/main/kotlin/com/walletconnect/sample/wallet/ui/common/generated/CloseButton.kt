package com.walletconnect.sample.wallet.ui.common.generated

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.walletconnect.sample.wallet.R

@Composable
fun CloseButton(modifier: Modifier = Modifier) {
    Image(painter = painterResource(R.drawable.close_button), contentDescription = null, modifier = modifier)
}

