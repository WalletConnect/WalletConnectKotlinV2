package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.web3inbox.domain.EthAccount

@Composable
fun RandomAccountSection(random: EthAccount.Random, onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        textAlign = TextAlign.Center,
        text = "This is the way W3I SDK works\nNote: Generates and saves random account for this app"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Random account")
            Text(fontSize = 8.sp, text = random.address)
        }
    }
}

@Composable
fun BurnerAccountSection(onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        textAlign = TextAlign.Center,
        text = "For Advanced testers.\nNote: This might have issues with persisting data. Please use Random one"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Burner account")
            Text(fontSize = 8.sp, text = EthAccount.Burner.address)
        }
    }
}

@Composable
fun FixedAccountSection(onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        textAlign = TextAlign.Center,
        text = "For Advanced testers.\nNote: This might have issues if too many ppl use it. Please use Random one"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Fixed account")
            Text(fontSize = 8.sp, text = EthAccount.Fixed.address)
        }
    }
}

@Composable
fun HorizontalLineDivider() {
    Spacer(modifier = Modifier.height(20.dp))
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .alpha(0.5f)
            .background(Color.Black)
    )
    Spacer(modifier = Modifier.height(20.dp))
}