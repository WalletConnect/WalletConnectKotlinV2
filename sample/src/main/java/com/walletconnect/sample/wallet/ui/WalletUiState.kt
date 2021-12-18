package com.walletconnect.sample.wallet.ui

import com.walletconnect.walletconnectv2.client.WalletConnectClientData

sealed class WalletUiEvent
data class InitSessionsList(val sessions: List<WalletConnectClientData.SettledSession>) : WalletUiEvent()
data class ShowSessionProposalDialog(val proposal: WalletConnectClientData.SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<WalletConnectClientData.SettledSession>, val message: String? = null) : WalletUiEvent()
data class ShowSessionRequestDialog(
    val sessionRequest: WalletConnectClientData.SessionRequest,
    val session: WalletConnectClientData.SettledSession
) : WalletUiEvent()

object PingSuccess : WalletUiEvent()
object RejectSession : WalletUiEvent()