package com.walletconnect.sample.wallet.ui.state

sealed class PairingState {
    object Loading : PairingState()
    object Success : PairingState()
    data class Error(val message: String) : PairingState()
    object Idle : PairingState()
}
