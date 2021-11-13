package org.walletconnect.example.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.walletconnect.example.wallet.ui.*
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.client.*

class WalletViewModel : ViewModel(), WalletConnectClientListener {
    private var _eventFlow = MutableSharedFlow<WalletUiEvent>()
    val eventFlow = _eventFlow.asLiveData()

    val settledSessions: MutableList<WalletConnectClientData.SettledSession> = mutableListOf()
    lateinit var proposal: WalletConnectClientData.SessionProposal

    fun pair(uri: String) {
        val pairParams = ClientTypes.PairParams(uri.trim())
        WalletConnectClient.pair(pairParams, this)
    }

    fun approve() {
        val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(proposal, accounts)
        WalletConnectClient.approve(approveParams)
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = proposal.topic

        val rejectParams: ClientTypes.RejectParams = ClientTypes.RejectParams(rejectionReason, proposalTopic)
        WalletConnectClient.reject(rejectParams)

        viewModelScope.launch {
            _eventFlow.emit(RejectSession)
        }
    }

    fun disconnect(topic: String, reason: String = "Reason") {
        val disconnectParams = ClientTypes.DisconnectParams(topic, reason)
        WalletConnectClient.disconnect(disconnectParams)
        removeSession(topic)

        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(settledSessions))
        }
    }

    override fun onSessionProposal(proposal: WalletConnectClientData.SessionProposal) {
        viewModelScope.launch {
            this@WalletViewModel.proposal = proposal
            _eventFlow.emit(ShowSessionProposalDialog(this@WalletViewModel.proposal))
        }
    }

    override fun onSettledSession(session: WalletConnectClientData.SettledSession) {
        settledSessions += session
        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(settledSessions))
        }
    }

    override fun onSessionRequest(request: WalletConnectClientData.SessionRequest) {
        //TODO handle session request generic object
    }

    override fun onSessionDelete(topic: String, reason: String) {
        removeSession(topic)
        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(settledSessions))
        }
    }

    private fun removeSession(topic: String) {
        settledSessions.find { session -> session.topic == topic }?.also { session -> settledSessions.remove(session) }
    }
}