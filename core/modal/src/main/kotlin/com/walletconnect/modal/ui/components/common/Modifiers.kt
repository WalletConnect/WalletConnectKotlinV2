package com.walletconnect.modal.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp

fun Modifier.roundedClickable(
    enabled: Boolean = true,
    bounded: Boolean = false,
    radius: Dp = Dp.Unspecified,
    onClick: () -> Unit
) = composed { clickable(
    enabled = enabled,
    indication = rememberRipple(bounded = bounded, radius = radius),
    interactionSource = remember { MutableInteractionSource() },
    onClick = onClick
) }
