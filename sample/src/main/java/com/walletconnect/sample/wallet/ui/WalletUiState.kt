package com.walletconnect.sample.wallet.ui

import com.walletconnect.walletconnectv2.client.WalletConnect

sealed class WalletUiEvent
data class InitSessionsList(val sessions: List<WalletConnect.Model.SettledSession>) : WalletUiEvent()
data class ShowSessionProposalDialog(val proposal: WalletConnect.Model.SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<WalletConnect.Model.SettledSession>, val message: String? = null) : WalletUiEvent()
data class ShowSessionRequestDialog(val sessionRequest: WalletConnect.Model.SessionRequest, val session: WalletConnect.Model.SettledSession) : WalletUiEvent()
object PingSuccess : WalletUiEvent()
object RejectSession : WalletUiEvent()