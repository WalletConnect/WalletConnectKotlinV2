package org.walletconnect.example.wallet.ui

import org.walletconnect.walletconnectv2.client.SessionProposal
import org.walletconnect.walletconnectv2.client.SettledSession

sealed class WalletUiEvent
data class ShowSessionProposalDialog(val proposal: SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<SettledSession>) : WalletUiEvent()
object RejectSession : WalletUiEvent()