package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.account.generateAvatarColors
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.components.internal.commons.button.TextButton
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.toVisibleAddress

enum class AccountButtonType {
    NORMAL, MIXED
}

internal sealed class AccountButtonState {
    object Loading : AccountButtonState()

    data class Normal(val address: String) : AccountButtonState()

    data class Mixed(
        val address: String,
        val chainImageUrl: String,
        val balance: String
    ) : AccountButtonState()

    object Invalid : AccountButtonState()
}

@Composable
fun AccountButton(
    web3ButtonState: Web3ModalState, accountButtonType: AccountButtonType
) {
    var state by remember { mutableStateOf<AccountButtonState>(AccountButtonState.Loading) }

    LaunchedEffect(Unit) {
        val data = web3ButtonState.getAccountButtonState(accountButtonType)
        state = data ?: AccountButtonState.Invalid
    }

    AccountButtonState(
        state = state,
        onClick = web3ButtonState::openWeb3Modal
    )
}

@Composable
private fun AccountButtonState(
    state: AccountButtonState,
    onClick: () -> Unit,
) {
    when (state) {
        AccountButtonState.Invalid -> UnavailableSession()
        AccountButtonState.Loading -> LoadingButton()
        is AccountButtonState.Normal -> AccountButtonNormal(
            address = state.address, onClick = onClick
        )

        is AccountButtonState.Mixed -> AccountButtonMixed(
            address = state.address,
            chainImageUrl = state.chainImageUrl,
            balance = state.balance,
            onClick = onClick
        )
    }
}

@Composable
private fun AccountButtonMixed(
    address: String,
    chainImageUrl: String,
    balance: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    ProvideWeb3ModalThemeComposition {
        val backgroundColor: Color
        val borderColor: Color
        val textColor: Color

        if (isEnabled) {
            backgroundColor = Web3ModalTheme.colors.overlay02
            borderColor = Web3ModalTheme.colors.overlay05
            textColor = Web3ModalTheme.colors.foreground.color100
        } else {
            backgroundColor = Web3ModalTheme.colors.overlay15
            borderColor = Web3ModalTheme.colors.overlay05
            textColor = Web3ModalTheme.colors.overlay15
        }

        Box(
            modifier = Modifier
                .height(40.dp)
                .background(backgroundColor, shape = RoundedCornerShape(100))
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(100))
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleNetworkImage(url = chainImageUrl, size = 24.dp, isEnabled = isEnabled)
                HorizontalSpacer(width = 6.dp)
                Text(text = balance, style = Web3ModalTheme.typo.paragraph600.copy(color = textColor))
                HorizontalSpacer(width = 8.dp)
                ImageButton(
                    text = address.toVisibleAddress(), image = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(width = 1.dp, color = Web3ModalTheme.colors.overlay05, shape = CircleShape)
                                .padding(1.dp)
                                .background(brush = Brush.linearGradient(generateAvatarColors(address)), shape = CircleShape)
                        )
                    },
                    isEnabled = isEnabled,
                    style = ButtonStyle.ACCOUNT,
                    size = ButtonSize.ACCOUNT_S,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun AccountButtonNormal(
    address: String, onClick: () -> Unit, isEnabled: Boolean = true
) {
    ProvideWeb3ModalThemeComposition {
        ImageButton(
            text = address.toVisibleAddress(), image = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(width = 1.dp, color = Web3ModalTheme.colors.overlay05, shape = CircleShape)
                        .padding(1.dp)
                        .background(brush = Brush.linearGradient(generateAvatarColors(address)), shape = CircleShape)
                )
            }, isEnabled = isEnabled, style = ButtonStyle.ACCOUNT, size = ButtonSize.ACCOUNT_M, onClick = onClick
        )
    }
}


@Composable
private fun UnavailableSession() {
    ProvideWeb3ModalThemeComposition {
        TextButton(text = "Session Unavailable", style = ButtonStyle.ACCOUNT, size = ButtonSize.M, isEnabled = false, onClick = {})
    }
}

@UiModePreview
@Composable
private fun UnavailableSessionPreview() {
    ComponentPreview {
        UnavailableSession()
    }
}

@UiModePreview
@Composable
private fun AccountButtonNormalPreview() {
    MultipleComponentsPreview({ AccountButtonNormal(address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}) },
        { AccountButtonNormal(address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}, isEnabled = false) })
}

@UiModePreview
@Composable
private fun AccountButtonMixedPreview() {
    MultipleComponentsPreview({ AccountButtonMixed(balance = "0.527 ETH", chainImageUrl = "", address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}) },
        { AccountButtonMixed(balance = "0.527 ETH", chainImageUrl = "", address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}, isEnabled = false) })
}
