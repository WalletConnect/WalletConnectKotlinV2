package com.walletconnect.sample.wallet.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.sample_common.ui.themedColor


@Composable
fun InnerContent(content: @Composable () -> Unit) {
    val sectionShape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .clip(sectionShape)
            .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = .1f), shape = sectionShape)
            .background(color = themedColor(darkColor = Color(0xFF282a2a), lightColor = Color(0xFFFFFFFF)))
            .padding(horizontal = 5.dp),
    ) {
        content()
    }
}
