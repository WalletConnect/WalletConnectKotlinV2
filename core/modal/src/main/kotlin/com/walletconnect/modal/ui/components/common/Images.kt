package com.walletconnect.modal.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ClickableImage(
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    Image(
        imageVector = imageVector,
        colorFilter = ColorFilter.tint(tint),
        contentDescription = contentDescription,
        modifier = modifier.then(Modifier.roundedClickable(onClick = onClick).padding(4.dp))
    )
}
