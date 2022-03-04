package com.walletconnect.sample.wallet.ui

import com.walletconnect.walletconnectv2.client.WalletConnect

sealed class WalletUiEvent
data class InitSessionsList(val sessions: List<WalletConnect.Model.Session>) : WalletUiEvent()
data class ShowSessionProposalDialog(val proposal: WalletConnect.Model.SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<WalletConnect.Model.Session>) : WalletUiEvent()
data class ShowSessionRequestDialog(
    val sessionRequest: WalletConnect.Model.SessionRequest,
    val session: WalletConnect.Model.Session
) : WalletUiEvent()

object Ping : WalletUiEvent()
object RejectSession : WalletUiEvent()