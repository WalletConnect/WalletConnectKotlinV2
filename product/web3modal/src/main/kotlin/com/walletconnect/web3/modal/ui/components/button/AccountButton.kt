package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.TransparentSurface
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
import com.walletconnect.web3.modal.utils.getImageData
import com.walletconnect.web3.modal.utils.toVisibleAddress

@Deprecated("com.walletconnect.web3.modal.ui.components.AccountButtonType has been deprecated. Please use com.reown.appkit.modal.ui.components.AccountButtonType instead from - https://github.com/reown-com/reown-kotlin")
enum class AccountButtonType {
    NORMAL, MIXED
}

internal sealed class AccountButtonState {
    object Loading : AccountButtonState()

    data class Normal(val address: String) : AccountButtonState()

    data class Mixed(
        val address: String,
        val chainImage: Modal.Model.ChainImage,
        val chainName: String,
        val balance: String?
    ) : AccountButtonState()

    object Invalid : AccountButtonState()
}

@Deprecated("com.walletconnect.web3.modal.ui.components.AccountButton has been deprecated. Please use com.reown.appkit.modal.ui.components.AccountButton instead from - https://github.com/reown-com/reown-kotlin")
@Composable
fun AccountButton(
    state: Web3ModalState,
    accountButtonType: AccountButtonType = AccountButtonType.NORMAL
) {
    val accountState by when (accountButtonType) {
        AccountButtonType.NORMAL -> state.accountNormalButtonState.collectAsState()
        AccountButtonType.MIXED -> state.accountMixedButtonState.collectAsState()
    }
    AccountButtonState(
        state = accountState,
        onClick = state::openWeb3Modal
    )
}

@Composable
internal fun AccountButtonState(
    state: AccountButtonState,
    onClick: () -> Unit,
) {
    when (state) {
        AccountButtonState.Invalid -> UnavailableSession()
        AccountButtonState.Loading -> LoadingButton()
        is AccountButtonState.Normal -> AccountButtonNormal(address = state.address, onClick = onClick)
        is AccountButtonState.Mixed -> AccountButtonMixed(
            address = state.address,
            chainImage = state.chainImage,
            chainData = state.balance ?: state.chainName,
            onClick = onClick
        )
    }
}

@Composable
private fun AccountButtonMixed(
    address: String,
    chainImage: Modal.Model.ChainImage,
    chainData: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    ProvideWeb3ModalThemeComposition {
        val backgroundColor: Color
        val borderColor: Color
        val textColor: Color

        if (isEnabled) {
            backgroundColor = Web3ModalTheme.colors.grayGlass02
            borderColor = Web3ModalTheme.colors.grayGlass05
            textColor = Web3ModalTheme.colors.foreground.color100
        } else {
            backgroundColor = Web3ModalTheme.colors.grayGlass15
            borderColor = Web3ModalTheme.colors.grayGlass05
            textColor = Web3ModalTheme.colors.grayGlass15
        }

        TransparentSurface(shape = RoundedCornerShape(100)) {
            Box(
                modifier = Modifier
                    .clickable(isEnabled) { onClick() }
                    .border(width = 1.dp, color = borderColor, shape = CircleShape)
                    .height(40.dp)
                    .background(backgroundColor)
            ) {
                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp).fillMaxHeight(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleNetworkImage(data = chainImage.getImageData(), size = 24.dp, isEnabled = isEnabled)
                    HorizontalSpacer(width = 4.dp)
                    Text(text = chainData, style = Web3ModalTheme.typo.paragraph600.copy(color = textColor))
                    HorizontalSpacer(width = 8.dp)
                    ImageButton(
                        text = address.toVisibleAddress(), image = {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(width = 2.dp, color = Web3ModalTheme.colors.grayGlass05, shape = CircleShape)
                                    .padding(2.dp)
                                    .background(brush = Brush.linearGradient(generateAvatarColors(address)), shape = CircleShape)
                            )
                        },
                        paddingValues = PaddingValues(start = 4.dp, end = 8.dp),
                        isEnabled = isEnabled,
                        style = ButtonStyle.ACCOUNT,
                        size = ButtonSize.ACCOUNT_S,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountButtonNormal(
    address: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    ProvideWeb3ModalThemeComposition {
        ImageButton(
            text = address.toVisibleAddress(), image = {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(width = 2.dp, color = Web3ModalTheme.colors.grayGlass05, shape = CircleShape)
                        .padding(2.dp)
                        .background(brush = Brush.linearGradient(generateAvatarColors(address)), shape = CircleShape)
                )
            },
            paddingValues = PaddingValues(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            isEnabled = isEnabled,
            style = ButtonStyle.ACCOUNT,
            size = ButtonSize.ACCOUNT_S,
            onClick = onClick
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
    MultipleComponentsPreview(
        { AccountButtonNormal(address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}) },
        { AccountButtonNormal(address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}, isEnabled = false) }
    )
}

@UiModePreview
@Composable
private fun AccountButtonMixedPreview() {
    MultipleComponentsPreview(
        { AccountButtonMixed(chainData = "ETH", chainImage = Modal.Model.ChainImage.Asset(R.drawable.ic_select_network), address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}) },
        { AccountButtonMixed(chainData = "ETH", chainImage = Modal.Model.ChainImage.Asset(R.drawable.ic_select_network), address = "0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb", onClick = {}, isEnabled = false) }
    )
}
