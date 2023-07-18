package com.walletconnect.wcmodal.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.walletconnect.wcmodal.ui.theme.ModalTheme

@Composable
internal fun ImageWithMainTint(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        imageVector = ImageVector.vectorResource(id = icon),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(ModalTheme.colors.main)
    )
}
