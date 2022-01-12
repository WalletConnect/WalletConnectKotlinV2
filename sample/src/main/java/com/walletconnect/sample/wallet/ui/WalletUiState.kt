package com.walletconnect.sample.wallet.ui

import com.walletconnect.walletconnectv2.client.SessionProposal
import com.walletconnect.walletconnectv2.client.SessionRequest
import com.walletconnect.walletconnectv2.client.SettledSession

sealed class WalletUiEvent
data class InitSessionsList(val sessions: List<SettledSession>) : WalletUiEvent()
data class ShowSessionProposalDialog(val proposal: SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<SettledSession>, val message: String? = null) : WalletUiEvent()
data class ShowSessionRequestDialog(val sessionRequest: SessionRequest, val session: SettledSession) : WalletUiEvent()
object PingSuccess : WalletUiEvent()
object RejectSession : WalletUiEvent()