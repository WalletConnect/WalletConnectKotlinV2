package com.walletconnect.web3.modal.ui.components.internal.snackbar

enum class SnackBarEventType {
    SUCCESS, CANCEL, ERROR
}

data class SnackBarEvent(
    val type: SnackBarEventType,
    val message: String
)
