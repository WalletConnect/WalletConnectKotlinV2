package com.walletconnect.sample.wallet.ui.state

sealed class PairingEvent {
    data class Error(val message: String) : PairingEvent()
    data class Expired(val message: String) : PairingEvent()
    data class ProposalExpired(val message: String) : PairingEvent()
}
