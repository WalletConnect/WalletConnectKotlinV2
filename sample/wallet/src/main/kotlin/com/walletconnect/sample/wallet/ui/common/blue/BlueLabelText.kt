package com.walletconnect.sample.wallet.ui.common.blue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.common.ui.themedColor


@Composable
fun BlueLabelText(value: String) {
    Text(
        text = value,
        maxLines = 1,
        style = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = themedColor(darkColor = Color(0xff1cc6ff), lightColor = Color(0xFF05ace5))
        ),
        modifier = Modifier
            .wrapContentWidth()
            .clip(CircleShape)
            .background(color = themedColor(darkColor = Color(0xFF153B47), lightColor = Color(0xFFDDF1F8)))
            .padding(start = 8.dp, top = 3.dp, end = 8.dp, bottom = 5.dp)
    )
}