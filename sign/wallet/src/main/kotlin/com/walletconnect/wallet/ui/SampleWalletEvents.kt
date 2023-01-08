package com.walletconnect.wallet.ui

sealed class SampleWalletEvents {

    // Sign
    object SessionProposal : SampleWalletEvents()

    data class PingSuccess(val topic: String, val timestamp: Long) : SampleWalletEvents()

    data class PingError(val timestamp: Long) : SampleWalletEvents()

    object Disconnect : SampleWalletEvents()

    data class SessionRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int) : SampleWalletEvents()

    object SessionRequestResponded : SampleWalletEvents()

    // Push
    data class PushRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int): SampleWalletEvents()

    object PushRequestResponded : SampleWalletEvents()

    data class PushMessage(val title: String, val body: String, val icon: String, val url: String): SampleWalletEvents()

    object NoAction : SampleWalletEvents()
}