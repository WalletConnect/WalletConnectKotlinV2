package com.walletconnect.sample.wallet.ui

sealed interface Web3WalletEvent

object NoAction : Web3WalletEvent, NotifyEvent

interface SignEvent : Web3WalletEvent {
    object SessionProposal : SignEvent
    object SessionAuthenticate : SignEvent
    data class SessionRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int) : SignEvent
    object ExpiredRequest : SignEvent
    object Disconnect : SignEvent
    data class ConnectionState(val isAvailable: Boolean) : SignEvent
}

interface AuthEvent : Web3WalletEvent {
    data class OnRequest(val id: Long, val message: String) : AuthEvent
}

