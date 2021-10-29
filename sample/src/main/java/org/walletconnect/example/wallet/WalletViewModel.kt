package org.walletconnect.example.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.walletconnect.example.wallet.ui.Session
import org.walletconnect.example.wallet.ui.ShowSessionProposalDialog
import org.walletconnect.example.wallet.ui.UpdateActiveSessions
import org.walletconnect.example.wallet.ui.WalletUiEvent
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.SessionProposal

class WalletViewModel : ViewModel() {

    private var _eventFlow = MutableSharedFlow<WalletUiEvent>()
    val eventFlow = _eventFlow.asLiveData()

    val activeSessions: MutableList<Session> = mutableListOf()
    lateinit var sessionProposal: SessionProposal

    fun pair(uri: String) {
        val pairParams = ClientTypes.PairParams(uri.trim())
        WalletConnectClient.pair(pairParams) { sessionProposal ->
            viewModelScope.launch {
                this@WalletViewModel.sessionProposal = sessionProposal
                _eventFlow.emit(ShowSessionProposalDialog(sessionProposal))
            }
        }
    }

    fun approve() {
        val session = Session(
            name = sessionProposal.name,
            uri = sessionProposal.dappUrl,
            icon = sessionProposal.icon.first().toString()
        )

        activeSessions += session

        val accounts = sessionProposal.chains.map { chainId ->
            "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"
        }

        WalletConnectClient.approve(accounts, sessionProposal)

        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(activeSessions))
        }
    }

    fun reject() {
        WalletConnectClient.reject("Reason", sessionProposal)
    }
}