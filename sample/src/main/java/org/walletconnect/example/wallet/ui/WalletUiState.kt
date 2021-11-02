package org.walletconnect.example.wallet.ui

import org.walletconnect.walletconnectv2.client.SessionProposal

sealed class WalletUiEvent
data class ShowSessionProposalDialog(val proposal: SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<Session>) : WalletUiEvent()
object RejectSession : WalletUiEvent()

data class Session(
    var icon: String = "",
    var name: String = "",
    var uri: String = ""
)