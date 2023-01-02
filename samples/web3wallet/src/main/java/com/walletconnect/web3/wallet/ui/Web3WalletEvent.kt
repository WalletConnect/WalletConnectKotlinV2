package com.walletconnect.web3.wallet.ui

sealed interface Web3WalletEvent

object NoAction : Web3WalletEvent

interface CoreEvent: Web3WalletEvent {
    object Disconnect : CoreEvent
}

interface SignEvent: Web3WalletEvent {
    object SessionProposal: SignEvent
    data class SessionRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int) : SignEvent
    object Disconnect : SignEvent
}


interface AuthEvent: Web3WalletEvent {
    data class OnRequest(val id: Long, val message: String) : Web3WalletEvent
}

