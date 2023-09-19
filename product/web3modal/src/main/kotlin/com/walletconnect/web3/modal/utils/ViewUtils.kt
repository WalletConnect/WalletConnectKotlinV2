package com.walletconnect.web3.modal.utils

import com.walletconnect.web3.modal.ui.components.button.AccountButtonType
import com.walletconnect.web3.modal.ui.components.button.ConnectButtonSize

internal fun Int.toConnectButtonSize() = when (this) {
    1 -> ConnectButtonSize.SMALL
    else -> ConnectButtonSize.NORMAL
}

internal fun Int.toAccountButtonType() = when(this) {
    1 -> AccountButtonType.MIXED
    else -> AccountButtonType.NORMAL
}
