package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingSpinner
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.StyledButton
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition

@Composable
internal fun LoadingButton() {
    ProvideWeb3ModalThemeComposition {
        StyledButton(style = ButtonStyle.ACCOUNT, size = ButtonSize.M, onClick = {}) {
            Box(
                modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center
            ) {
                LoadingSpinner(size = 16.dp)
            }
        }
    }
}

@UiModePreview
@Composable
private fun LoadingButtonPreview() {
    ComponentPreview {
        LoadingButton()
    }
}
