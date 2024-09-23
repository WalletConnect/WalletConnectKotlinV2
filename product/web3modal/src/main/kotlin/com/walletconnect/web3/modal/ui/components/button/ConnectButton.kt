package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingSpinner
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.components.internal.commons.button.TextButton
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition

@Deprecated("com.walletconnect.web3.modal.ui.components.ConnectButtonSize has been deprecated. Please use com.reown.appkit.modal.ui.components.ConnectButtonSize instead from - https://github.com/reown-com/reown-kotlin")
enum class ConnectButtonSize {
    NORMAL, SMALL
}

@Deprecated("com.walletconnect.web3.modal.ui.components.ConnectButton has been deprecated. Please use com.reown.appkit.modal.ui.components.ConnectButton instead from - https://github.com/reown-com/reown-kotlin")
@Composable
fun ConnectButton(
    state: Web3ModalState,
    buttonSize: ConnectButtonSize = ConnectButtonSize.NORMAL
) {
    val isLoading: Boolean by state.isOpen.collectAsState(initial = false)
    val isConnected: Boolean by state.isConnected.collectAsState(initial = false)

    ConnectButton(
        size = buttonSize,
        isLoading = isLoading,
        isEnabled = !isConnected
    ) {
        state.openWeb3Modal()
    }
}

@Composable
internal fun ConnectButton(
    size: ConnectButtonSize,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    ProvideWeb3ModalThemeComposition {
        val buttonSize = when (size) {
            ConnectButtonSize.NORMAL -> ButtonSize.M
            ConnectButtonSize.SMALL -> ButtonSize.S
        }
        if (isLoading && isEnabled) {
            ImageButton(
                text = "Connecting...",
                image = { LoadingSpinner(size = 10.dp, strokeWidth = 2.dp) },
                style = ButtonStyle.LOADING,
                size = buttonSize
            ) {}
        } else {
            val text = when (size) {
                ConnectButtonSize.NORMAL -> "Connect wallet"
                ConnectButtonSize.SMALL -> "Connect"
            }
            TextButton(
                text = text,
                style = ButtonStyle.MAIN,
                size = buttonSize,
                isEnabled = isEnabled,
                onClick = onClick
            )
        }
    }
}

@UiModePreview
@Composable
private fun ConnectButtonPreview() {
    MultipleComponentsPreview(
        { ConnectButton(size = ConnectButtonSize.NORMAL) {} },
        { ConnectButton(size = ConnectButtonSize.SMALL) {} },
    )
}

@UiModePreview
@Composable
private fun DisabledConnectButtonPreview() {
    MultipleComponentsPreview(
        { ConnectButton(size = ConnectButtonSize.NORMAL, isEnabled = false) {} },
        { ConnectButton(size = ConnectButtonSize.SMALL, isEnabled = false) {} },
    )
}

@UiModePreview
@Composable
private fun LoadingConnectButtonPreview() {
    MultipleComponentsPreview(
        { ConnectButton(size = ConnectButtonSize.NORMAL, isLoading = true) {} },
        { ConnectButton(size = ConnectButtonSize.SMALL, isLoading = true) {} },
    )
}
