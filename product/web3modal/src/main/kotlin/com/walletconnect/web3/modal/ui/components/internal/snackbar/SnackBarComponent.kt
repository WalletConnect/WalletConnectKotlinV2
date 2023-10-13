package com.walletconnect.web3.modal.ui.components.internal.snackbar

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow

internal val LocalSnackBarComponent = staticCompositionLocalOf<SnackBarComponent>() {
    error("CompositionLocal SnackBarComponent not present")
}

interface SnackBar {
    fun show(snackBarEvent: SnackBarEvent)
}

internal class SnackBarComponent: SnackBar {

    private val snackBarSharedFlow = MutableSharedFlow<SnackBarEvent>()

    override fun show(snackBarEvent: SnackBarEvent) {
        snackBarSharedFlow.tryEmit(snackBarEvent)
    }
}