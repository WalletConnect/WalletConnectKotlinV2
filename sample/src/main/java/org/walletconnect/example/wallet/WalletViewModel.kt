package org.walletconnect.example.wallet

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.walletconnect.example.wallet.ui.*
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.SessionProposal
import org.walletconnect.walletconnectv2.client.SettledSession
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener

class WalletViewModel : ViewModel(), WalletConnectClientListener {
    private var _eventFlow = MutableSharedFlow<WalletUiEvent>()
    val eventFlow = _eventFlow.asLiveData()

    val settledSessions: MutableList<SettledSession> = mutableListOf()
    lateinit var proposal: SessionProposal

    fun pair(uri: String) {
        val pairParams = ClientTypes.PairParams(uri.trim())
        WalletConnectClient.pair(pairParams, this)
    }

    fun approve() {
        val accounts =
            proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(proposal, accounts)
        WalletConnectClient.approve(approveParams)
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = proposal.topic

        val rejectParams: ClientTypes.RejectParams =
            ClientTypes.RejectParams(rejectionReason, proposalTopic)
        WalletConnectClient.reject(rejectParams)

        viewModelScope.launch {
            _eventFlow.emit(RejectSession)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun disconnect(topic: String, reason: String = "Reason") {
        val disconnectParams = ClientTypes.DisconnectParams(topic, reason)
        WalletConnectClient.disconnect(disconnectParams)

        settledSessions.removeIf {  session -> session.topic == topic }
        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(settledSessions))
        }
    }

    override fun onSessionProposal(proposal: SessionProposal) {
        viewModelScope.launch {
            this@WalletViewModel.proposal = proposal
            _eventFlow.emit(ShowSessionProposalDialog(this@WalletViewModel.proposal))
        }
    }

    override fun onSettledSession(session: SettledSession) {
        settledSessions += session
        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(settledSessions))
        }
    }

    override fun onSessionRequest(payload: Any) {
        //TODO handle session request generic object
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSessionDelete(topic: String, reason: String) {
        settledSessions.removeIf {  session -> session.topic == topic }
        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(settledSessions))
        }
    }
}