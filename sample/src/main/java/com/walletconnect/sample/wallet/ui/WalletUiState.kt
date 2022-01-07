package com.walletconnect.sample.wallet.ui

import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel

sealed class WalletUiEvent
data class InitSessionsList(val sessions: List<WalletConnectClientModel.SettledSession>) : WalletUiEvent()
data class ShowSessionProposalDialog(val proposal: WalletConnectClientModel.SessionProposal) : WalletUiEvent()
data class UpdateActiveSessions(val sessions: List<WalletConnectClientModel.SettledSession>, val message: String? = null) : WalletUiEvent()
data class ShowSessionRequestDialog(
    val sessionRequest: WalletConnectClientModel.SessionRequest,
    val session: WalletConnectClientModel.SettledSession
) : WalletUiEvent()

object PingSuccess : WalletUiEvent()
object RejectSession : WalletUiEvent()