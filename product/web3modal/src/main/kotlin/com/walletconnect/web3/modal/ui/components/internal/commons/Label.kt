package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun AllLabel(isEnabled: Boolean = true) {
    ListLabel(text = "ALL", isEnabled = isEnabled)
}

@Composable
internal fun TextLabel(text: String, isEnabled: Boolean = true) {
    ListLabel(
        text = text,
        isEnabled = isEnabled,
        backgroundColor = Web3ModalTheme.colors.overlay10,
        labelTextColor = Web3ModalTheme.colors.foreground.color150
    )
}

@Composable
internal fun GetWalletLabel(isEnabled: Boolean = true) {
    ListLabel(text = "GET WALLET", isEnabled = isEnabled)
}

@Composable
internal fun RecentLabel(isEnabled: Boolean = true) {
    ListLabel(
        text = "RECENT",
        isEnabled = isEnabled,
        backgroundColor = Web3ModalTheme.colors.overlay10,
        labelTextColor = Web3ModalTheme.colors.foreground.color150
    )
}

@Composable
internal fun InstalledLabel(isEnabled: Boolean = true) {
    ListLabel(
        text = "INSTALLED",
        isEnabled = isEnabled,
        backgroundColor = Web3ModalTheme.colors.success15,
        labelTextColor = Web3ModalTheme.colors.success
    )
}

@Composable
private fun ListLabel(
    text: String,
    isEnabled: Boolean,
    backgroundColor: Color = Web3ModalTheme.colors.main15,
    labelTextColor: Color = Web3ModalTheme.colors.main100
) {
    val textColor: Color
    val background: Color
    if (isEnabled) {
        background = backgroundColor
        textColor = labelTextColor
    } else {
        background = Web3ModalTheme.colors.overlay10
        textColor = Web3ModalTheme.colors.foreground.color300
    }
    Box(
        modifier = Modifier
            .background(background, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Text(text = text, style = Web3ModalTheme.typo.micro700.copy(textColor))
    }
}

@Composable
@UiModePreview
private fun AllLabelPreview() {
    MultipleComponentsPreview(
        { AllLabel() },
        { AllLabel(false) },
    )
}

@Composable
@UiModePreview
private fun TextLabelPreview() {
    MultipleComponentsPreview(
        { TextLabel("240+") },
        { TextLabel("240+", false) },
    )
}

@Composable
@UiModePreview
private fun GetWalletLabelPreview() {
    MultipleComponentsPreview(
        { GetWalletLabel() },
        { GetWalletLabel(false) },
    )
}

@Composable
@UiModePreview
private fun RecentLabelPreview() {
    MultipleComponentsPreview(
        { RecentLabel() },
        { RecentLabel(false) },
    )
}

@Composable
@UiModePreview
private fun InstalledLabelPreview() {
    MultipleComponentsPreview(
        { InstalledLabel() },
        { InstalledLabel(false) },
    )
}

