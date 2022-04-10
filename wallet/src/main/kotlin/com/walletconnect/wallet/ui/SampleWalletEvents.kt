package com.walletconnect.wallet.ui

import com.walletconnect.wallet.ui.sessions.active.ActiveSessionUI

sealed class SampleWalletEvents {

    object SessionProposal: SampleWalletEvents()

    data class ActiveSessions(val listOfActiveSessions: List<ActiveSessionUI>): SampleWalletEvents() {

        override fun equals(other: Any?): Boolean {
            return other is ActiveSessions && this.listOfActiveSessions == other.listOfActiveSessions
        }

        override fun hashCode(): Int {
            return listOfActiveSessions.hashCode()
        }
    }

    data class UpdateSessions(val listOfActiveSessions: List<ActiveSessionUI>): SampleWalletEvents()

    data class PingSuccess(val topic: String, val timestamp: Long): SampleWalletEvents()

    data class PingError(val timestamp: Long): SampleWalletEvents()

    object Disconnect: SampleWalletEvents()

    object SessionRequest: SampleWalletEvents()

    object NoAction : SampleWalletEvents()
}