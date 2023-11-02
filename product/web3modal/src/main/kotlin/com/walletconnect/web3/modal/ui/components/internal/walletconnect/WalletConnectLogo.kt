package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun WalletConnectLogo(
    isEnabled: Boolean = true
) {
    val background: Color
    val border: Color
    val colorFilter: ColorFilter?
    if (isEnabled) {
        background = Web3ModalTheme.colors.accent100
        border = Web3ModalTheme.colors.grayGlass10
        colorFilter = null
    } else {
        background = Web3ModalTheme.colors.background.color300
        border = Web3ModalTheme.colors.grayGlass05
        colorFilter = ColorFilter.tint(Web3ModalTheme.colors.grayGlass30)
    }

    Image(
        modifier = Modifier
            .size(40.dp)
            .background(background, shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(8.dp))
            .padding(4.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet_connect_logo),
        contentDescription = ContentDescription.WC_LOGO.description,

        colorFilter = colorFilter
    )
}

@UiModePreview
@Composable
private fun PreviewWalletConnectLogo() {
    MultipleComponentsPreview(
        { WalletConnectLogo() },
        { WalletConnectLogo(false) }
    )
}