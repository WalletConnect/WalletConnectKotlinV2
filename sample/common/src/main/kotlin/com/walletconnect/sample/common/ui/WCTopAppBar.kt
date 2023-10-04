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
import androidx.compose.ui.composed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun WCTopAppBar(
    titleText: String,
    @DrawableRes firstIcon: Int? = null,
    @DrawableRes secondIcon: Int? = null,
    onFirstIconClick: (() -> Unit)? = null,
    onSecondIconClick: (() -> Unit)? = null,
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
        firstIcon?.let {
            Image(
                modifier = Modifier.largerCircularClickable(24.dp) { onFirstIconClick?.invoke() },
                painter = painterResource(id = firstIcon),
                contentDescription = null,
            )
        }
        secondIcon?.let {
            Spacer(modifier = Modifier.width(24.dp))
            Image(
                modifier = Modifier.largerCircularClickable(24.dp) { onSecondIconClick?.invoke() },
                painter = painterResource(id = secondIcon),
                contentDescription = null,
            )
        }
    }
}

fun Modifier.largerCircularClickable(radius: Dp, onClick: () -> Unit) = composed {
    this.then(
        Modifier.clickable(
            indication = rememberRipple(bounded = false, radius = radius), interactionSource = remember { MutableInteractionSource() }, onClick = onClick
        )
    )
}