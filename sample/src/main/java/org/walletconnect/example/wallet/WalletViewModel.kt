package org.walletconnect.example.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.walletconnect.example.wallet.ui.*
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

        val proposerPublicKey: String = sessionProposal.proposerPublicKey
        val proposalTtl: Long = sessionProposal.ttl
        val proposalTopic: String = sessionProposal.topic
        val accounts = sessionProposal.chains.map { chainId ->
            "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"
        }
        val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(accounts, proposerPublicKey, proposalTtl, proposalTopic)

        WalletConnectClient.approve(approveParams)

        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(activeSessions))
        }
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = sessionProposal.topic
        val rejectParams: ClientTypes.RejectParams = ClientTypes.RejectParams(rejectionReason, proposalTopic)

        WalletConnectClient.reject(rejectParams)

        viewModelScope.launch {
            _eventFlow.emit(RejectSession)
        }
    }
}