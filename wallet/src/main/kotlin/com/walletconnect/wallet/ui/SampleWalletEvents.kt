package com.walletconnect.wallet.ui

sealed class SampleWalletEvents {

    object SessionProposal : SampleWalletEvents()

    data class PingSuccess(val topic: String, val timestamp: Long) : SampleWalletEvents()

    data class PingError(val timestamp: Long) : SampleWalletEvents()

    object Disconnect : SampleWalletEvents()

    object SessionRequest : SampleWalletEvents()

    object NoAction : SampleWalletEvents()
}