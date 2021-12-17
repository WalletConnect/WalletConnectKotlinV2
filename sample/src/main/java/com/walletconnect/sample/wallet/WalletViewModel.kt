package com.walletconnect.sample.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.wallet.ui.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.walletconnect.walletconnectv2.WalletConnectClient
import com.walletconnect.walletconnectv2.client.ClientTypes
import com.walletconnect.walletconnectv2.client.WalletConnectClientData
import com.walletconnect.walletconnectv2.client.WalletConnectClientListener
import com.walletconnect.walletconnectv2.client.WalletConnectClientListeners

class WalletViewModel : ViewModel(), WalletConnectClientListener {
    private var _eventFlow = MutableStateFlow<WalletUiEvent>(InitSessionsList(WalletConnectClient.getListOfSettledSessions()))
    val eventFlow: LiveData<WalletUiEvent> = _eventFlow.asLiveData()

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
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
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
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
            }

            override fun onError(error: Throwable) {
                //Session disconnect error
            }
        })
    }

    fun respondRequest(sessionRequest: WalletConnectClientData.SessionRequest) {
        val result = ClientTypes.ResponseParams(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnectClientData.JsonRpcResponse.JsonRpcResult(
                sessionRequest.request.id,
                "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
            )
        )

        WalletConnectClient.respond(result, object : WalletConnectClientListeners.SessionPayload {
            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun rejectRequest(sessionRequest: WalletConnectClientData.SessionRequest) {
        val result = ClientTypes.ResponseParams(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnectClientData.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                WalletConnectClientData.JsonRpcResponse.Error(500, "Kotlin Wallet Error")
            )
        )

        WalletConnectClient.respond(result, object : WalletConnectClientListeners.SessionPayload {
            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun sessionUpdate(session: WalletConnectClientData.SettledSession) {
        val updateParams = ClientTypes.UpdateParams(
            sessionTopic = session.topic,
            sessionState = WalletConnectClientData.SessionState(accounts = listOf("eip155:8001:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62"))
        )

        WalletConnectClient.update(updateParams, object : WalletConnectClientListeners.SessionUpdate {
            override fun onSuccess(updatedSession: WalletConnectClientData.UpdatedSession) {
                viewModelScope.launch {
                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session update"))
                }
            }

            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun sessionUpgrade(session: WalletConnectClientData.SettledSession) {
        val permissions =
            WalletConnectClientData.SessionPermissions(
                blockchain = WalletConnectClientData.Blockchain(chains = listOf("eip155:80001")),
                jsonRpc = WalletConnectClientData.Jsonrpc(listOf("eth_sign"))
            )
        val upgradeParams = ClientTypes.UpgradeParams(topic = session.topic, permissions = permissions)
        WalletConnectClient.upgrade(upgradeParams, object : WalletConnectClientListeners.SessionUpgrade {
            override fun onSuccess(upgradedSession: WalletConnectClientData.UpgradedSession) {
                viewModelScope.launch {
                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session upgrade"))
                }
            }

            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun sessionPing(session: WalletConnectClientData.SettledSession) {
        val pingParams = ClientTypes.PingParams(session.topic)

        WalletConnectClient.ping(pingParams, object : WalletConnectClientListeners.SessionPing {
            override fun onSuccess(topic: String) {
                viewModelScope.launch {
                    _eventFlow.emit(PingSuccess)
                }
            }

            override fun onError(error: Throwable) {
                //Error
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
        viewModelScope.launch {
            val session = WalletConnectClient.getListOfSettledSessions().find { session -> session.topic == sessionRequest.topic }!!
            _eventFlow.emit(ShowSessionRequestDialog(sessionRequest, session))
        }
    }

    override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {
        viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
    }

    override fun onSessionNotification(sessionNotification: WalletConnectClientData.SessionNotification) {
        //TODO handle session notification
    }
}