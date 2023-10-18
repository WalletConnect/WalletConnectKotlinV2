package com.walletconnect.web3.modal.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.walletconnect.web3.modal.ui.Web3ModalTheme

internal data class CustomComposition(
    val mode: Web3ModalTheme.Mode = Web3ModalTheme.Mode.AUTO,
    val lightColors: Web3ModalTheme.Colors = Web3ModalTheme.provideLightWeb3ModalColors(),
    val darkColors: Web3ModalTheme.Colors = Web3ModalTheme.provideDarkWeb3ModalColor(),
)

internal val LocalCustomComposition = compositionLocalOf { CustomComposition() }