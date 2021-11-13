package org.walletconnect.example.wallet.ui

import org.walletconnect.walletconnectv2.client.WalletConnectClientData

sealed class WalletUiEvent
data class ShowSessionProposalDialog(val proposal: WalletConnectClientData.SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<WalletConnectClientData.SettledSession>) : WalletUiEvent()
object RejectSession : WalletUiEvent()