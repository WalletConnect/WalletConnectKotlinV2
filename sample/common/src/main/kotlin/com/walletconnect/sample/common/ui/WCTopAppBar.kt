package com.walletconnect.sample.common.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun WCTopAppBar(
    titleText: String,
    vararg actionImages: TopBarActionImage,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titleText, style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        actionImages.forEachIndexed() { index, actionImage ->
            Image(
                modifier = Modifier.clickable(indication = rememberRipple(bounded = false, radius = 24.dp), interactionSource = remember { MutableInteractionSource() }, onClick = actionImage.onClick),
                painter = painterResource(id = actionImage.resource),
                contentDescription = null,
            )
            if (index != actionImages.lastIndex) Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

data class TopBarActionImage(
    @DrawableRes val resource: Int,
    val onClick: () -> Unit,
)

// TODO: Add Preview after merging https://github.com/WalletConnect/WalletConnectKotlinV2/pull/1176