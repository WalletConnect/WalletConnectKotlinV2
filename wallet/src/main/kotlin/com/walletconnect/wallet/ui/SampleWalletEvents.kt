package com.walletconnect.wallet.ui

sealed class SampleWalletEvents {

    object SessionProposal : SampleWalletEvents()

    data class PingSuccess(val topic: String, val timestamp: Long) : SampleWalletEvents()

    data class PingError(val timestamp: Long) : SampleWalletEvents()

    object Disconnect : SampleWalletEvents()

    data class SessionRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int) : SampleWalletEvents()

    object SessionRequestResponded : SampleWalletEvents()

    object NoAction : SampleWalletEvents()
}