package com.walletconnect.web3.modal.ui.components.internal.commons.entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.grayColorFilter
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun StoreEntry(
    text: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val background: Color
    val textColor: Color
    val colorFilter: ColorFilter?
    if (isEnabled) {
        textColor = Web3ModalTheme.colors.foreground.color200
        background = Web3ModalTheme.colors.overlay02
        colorFilter = null
    } else {
        textColor = Web3ModalTheme.colors.foreground.color300
        background = Web3ModalTheme.colors.overlay10
        colorFilter = grayColorFilter
    }
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable { onClick() }
                .background(background),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalSpacer(width = 10.dp)
            PlayStoreIcon(colorFilter = colorFilter)
            HorizontalSpacer(width = 10.dp)
            Text(text = text, style = Web3ModalTheme.typo.paragraph600.copy(color = textColor), modifier = Modifier.weight(1f))
            HorizontalSpacer(width = 10.dp)
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                contentDescription = ContentDescription.FORWARD_ARROW.description,
                colorFilter = ColorFilter.tint(textColor)
            )
            HorizontalSpacer(width = 18.dp)
        }
    }
}

@Composable
private fun PlayStoreIcon(colorFilter: ColorFilter?) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Web3ModalTheme.colors.overlay02, shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Web3ModalTheme.colors.overlay05, shape = RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_google_playstore),
            modifier = Modifier.size(36.dp),
            contentDescription = ContentDescription.STORE_IMAGE.description,
            colorFilter = colorFilter
        )
    }
}

@UiModePreview
@Composable
private fun PreviewStoreEntry() {
    MultipleComponentsPreview(
        { StoreEntry(text = "Get App") {} },
        { StoreEntry(text = "Get App", isEnabled = false) {} },
    )
}
