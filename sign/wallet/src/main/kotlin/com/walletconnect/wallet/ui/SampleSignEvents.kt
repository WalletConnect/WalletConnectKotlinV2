package com.walletconnect.wallet.ui

sealed class SampleSignEvents {

    object SessionProposal : SampleSignEvents()

    data class PingSuccess(val topic: String, val timestamp: Long) : SampleSignEvents()

    data class PingError(val timestamp: Long) : SampleSignEvents()

    object Disconnect : SampleSignEvents()

    data class SessionRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int) : SampleSignEvents()

    object SessionRequestResponded : SampleSignEvents()

    object NoAction : SampleSignEvents()
}