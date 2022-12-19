package com.walletconnect.showcase.ui

sealed interface ShowcaseEvent

object NoAction : ShowcaseEvent

interface CoreEvent: ShowcaseEvent {
    object Disconnect : CoreEvent
}

interface SignEvent: ShowcaseEvent {
    object SessionProposal: SignEvent
    data class SessionRequest(val arrayOfArgs: ArrayList<String?>, val numOfArgs: Int) : SignEvent
    object Disconnect : SignEvent
}


interface AuthEvent: ShowcaseEvent {
    data class OnRequest(val id: Long, val message: String) : ShowcaseEvent
}

