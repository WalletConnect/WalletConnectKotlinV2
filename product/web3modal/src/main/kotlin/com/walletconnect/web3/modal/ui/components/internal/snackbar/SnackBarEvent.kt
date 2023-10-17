package com.walletconnect.web3.modal.ui.components.internal.snackbar

internal enum class SnackBarEventType {
    SUCCESS, INFO, ERROR
}

internal interface SnackBarEvent {
    val type: SnackBarEventType
    val message: String
    val duration: SnackbarDuration

    fun dismiss()
}
enum class SnackBarResultState {
    Dismissed
}

enum class SnackbarDuration(val value: Long) {
    SHORT(2000L), LONG(4000L)
}
