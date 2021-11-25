package org.walletconnect.example.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.walletconnect.example.wallet.ui.RejectSession
import org.walletconnect.example.wallet.ui.ShowSessionProposalDialog
import org.walletconnect.example.wallet.ui.UpdateActiveSessions
import org.walletconnect.example.wallet.ui.WalletUiEvent
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners

class WalletViewModel : ViewModel(), WalletConnectClientListener {
    private var _eventFlow = MutableSharedFlow<WalletUiEvent>()
    val eventFlow = _eventFlow.asLiveData()

    val listOfSettledSessions: MutableList<WalletConnectClientData.SettledSession> = mutableListOf()
    private lateinit var proposal: WalletConnectClientData.SessionProposal

    init {
        WalletConnectClient.setWalletConnectListener(this)
    }

    fun pair(uri: String) {
        val pairParams = ClientTypes.PairParams(uri.trim())

        WalletConnectClient.pair(pairParams, object : WalletConnectClientListeners.Pairing {
            override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                //Settled pairing
            }

            override fun onError(error: Throwable) {
                //Pairing approval error
            }
        })
    }

    fun approve() {
        val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(proposal, accounts)

        WalletConnectClient.approve(approveParams, object : WalletConnectClientListeners.SessionApprove {
            override fun onSuccess(settledSession: WalletConnectClientData.SettledSession) {
                listOfSettledSessions += settledSession
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(listOfSettledSessions)) }
            }

            override fun onError(error: Throwable) {
                //Approve session error
            }
        })
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = proposal.topic
        val rejectParams: ClientTypes.RejectParams = ClientTypes.RejectParams(rejectionReason, proposalTopic)

        WalletConnectClient.reject(rejectParams, object : WalletConnectClientListeners.SessionReject {
            override fun onSuccess(rejectedSession: WalletConnectClientData.RejectedSession) {
                viewModelScope.launch { _eventFlow.emit(RejectSession) }
            }

            override fun onError(error: Throwable) {
                //Reject proposal error
            }
        })
    }

    fun disconnect(topic: String, reason: String = "Reason") {
        val disconnectParams = ClientTypes.DisconnectParams(topic, reason)

        WalletConnectClient.disconnect(disconnectParams, object : WalletConnectClientListeners.SessionDelete {
            override fun onSuccess(deletedSession: WalletConnectClientData.DeletedSession) {
                removeSession(deletedSession.topic)
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(listOfSettledSessions)) }
            }

            override fun onError(error: Throwable) {
                //Session disconnect error
            }
        })
    }

    override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
        viewModelScope.launch {
            this@WalletViewModel.proposal = sessionProposal
            _eventFlow.emit(ShowSessionProposalDialog(this@WalletViewModel.proposal))
        }
    }

    override fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest) {
        //TODO handle session request generic object
    }

    override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {
        removeSession(deletedSession.topic)
        viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(listOfSettledSessions)) }
    }

    private fun removeSession(topic: String) {
        listOfSettledSessions.find { session -> session.topic == topic }?.also { session -> listOfSettledSessions.remove(session) }
    }
}