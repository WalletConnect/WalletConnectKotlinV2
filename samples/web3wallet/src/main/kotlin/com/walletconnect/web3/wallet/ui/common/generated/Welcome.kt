package com.walletconnect.web3.wallet.ui.common.generated

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.walletconnect.web3.wallet.ui.common.themedColor

@Composable
fun Welcome(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        WelcomeTitle()
        Spacer(modifier = Modifier.height(10.dp))
        WelcomeContent()
    }
}


@Composable
fun WelcomeTitle() {
    Text(
        text = "Welcome",
        style = TextStyle(
            fontSize = 34.0.sp,
            color = themedColor(darkColor = Color(alpha = 255, red = 241, green = 243, blue = 243), lightColor = Color(alpha = 255, red = 20, green = 20, blue = 20)),
            lineHeight = 1.205882339477539.em,
            letterSpacing = 0.37400001287460327.sp,
            fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
    )
}

@Composable
fun WelcomeContent() {
    Text(
        text = "We made this Example Wallet App to help developers integrate the WalletConnect SDK and provide an amazing experience to their users.",
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 15.0.sp,
            color = themedColor(darkColor = Color(alpha = 255, red = 121, green = 133, blue = 133), lightColor = Color(alpha = 255, red = 121, green = 133, blue = 133)),
            lineHeight = 1.4.em,
            fontWeight = FontWeight.Medium,
        ),
    )
}