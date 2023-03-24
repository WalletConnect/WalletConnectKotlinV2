package com.walletconnect.sample.wallet.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun Content(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .clip(shape = RoundedCornerShape(25.dp))
            .fillMaxWidth()
            .background(themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .12f), lightColor = Color(0xFF505059).copy(.1f)))
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = title,
            style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = themedColor(darkColor = Color(0xFF413F3F), lightColor = Color(0xFFE4E2E7).copy(0.9f))),
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 15.dp)
                .clip(CircleShape)
                .background(color = themedColor(darkColor = Color(0xFFE4E4E7).copy(.33f), lightColor = Color(0xFF8a8498).copy(.66f)))
                .padding(vertical = 5.dp, horizontal = 8.dp)
        )
        content()
        Spacer(modifier = Modifier.height(5.dp))
    }
    Spacer(modifier = Modifier.height(5.dp))
}