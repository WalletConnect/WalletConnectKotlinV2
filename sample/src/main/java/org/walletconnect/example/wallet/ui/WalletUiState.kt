package org.walletconnect.example.wallet.ui

import org.walletconnect.example.R

sealed class WalletUiEvent
data class ShowSessionProposalDialog(val proposal: SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<Session>) : WalletUiEvent()

data class SessionProposal(
    var icon: Int = R.drawable.ic_walletconnect_circle_blue,
    var name: String = "",
    var uri: String = "",
    var description: String = "",
    var chains: List<String> = emptyList(),
    var methods: List<String> = emptyList()
)

data class Session(
    var icon: Int = R.drawable.ic_walletconnect_circle_blue,
    var name: String = "",
    var uri: String = ""
)