package com.walletconnect.sample.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.wallet.ui.*
import com.walletconnect.walletconnectv2.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel(), WalletConnectClient.Delegate {
    private var _eventFlow = MutableStateFlow<WalletUiEvent>(InitSessionsList(WalletConnectClient.getListOfSettledSessions()))
    val eventFlow: LiveData<WalletUiEvent> = _eventFlow.asLiveData()

    private lateinit var proposal: SessionProposal

    init {
        WalletConnectClient.setDelegate(this)
    }

    fun pair(uri: String) {
        val pair = Pair(uri.trim())
        WalletConnectClient.pair(pair, object : Pairing {
            override fun onSuccess(settledPairing: SettledPairing) {
                //Settled pairing
            }

            override fun onError(error: Throwable) {
                //Pairing approval error
            }
        })
    }

    fun approve() {
        val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approve = Approve(proposal, accounts)

        WalletConnectClient.approve(approve, object : SessionApprove {
            override fun onSuccess(settledSession: SettledSession) {
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
        val reject = Reject(rejectionReason, proposalTopic)

        WalletConnectClient.reject(reject, object : SessionReject {
            override fun onSuccess(rejectedSession: RejectedSession) {
                viewModelScope.launch { _eventFlow.emit(RejectSession) }
            }

            override fun onError(error: Throwable) {
                //Reject proposal error
            }
        })
    }

    fun disconnect(topic: String, reason: String = "Reason") {
        val disconnect = Disconnect(topic, reason)

        WalletConnectClient.disconnect(disconnect, object : SessionDelete {
            override fun onSuccess(deletedSession: DeletedSession) {
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
            }

            override fun onError(error: Throwable) {
                //Session disconnect error
            }
        })
    }

    fun respondRequest(sessionRequest: SessionRequest) {
        val response = Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = JsonRpcResponse.JsonRpcResult(
                sessionRequest.request.id,
                "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
            )
        )

        WalletConnectClient.respond(response, object : SessionPayload {
            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun rejectRequest(sessionRequest: SessionRequest) {
        val response = Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                JsonRpcResponse.Error(500, "Kotlin Wallet Error")
            )
        )

        WalletConnectClient.respond(response, object : SessionPayload {
            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun sessionUpdate(session: SettledSession) {
        val update = Update(
            sessionTopic = session.topic,
            sessionState = SessionState(accounts = listOf("eip155:8001:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62"))
        )

        WalletConnectClient.update(update, object : SessionUpdate {
            override fun onSuccess(updatedSession: UpdatedSession) {
                viewModelScope.launch {
                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session update"))
                }
            }

            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun sessionUpgrade(session: SettledSession) {
        val permissions =
            SessionPermissions(
                blockchain = Blockchain(chains = listOf("eip155:80001")),
                jsonRpc = Jsonrpc(listOf("eth_sign"))
            )
        val upgrade = Upgrade(topic = session.topic, permissions = permissions)

        WalletConnectClient.upgrade(upgrade, object : SessionUpgrade {
            override fun onSuccess(upgradedSession: UpgradedSession) {
                viewModelScope.launch {
                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session upgrade"))
                }
            }

            override fun onError(error: Throwable) {
                //Error
            }
        })
    }

    fun sessionPing(session: SettledSession) {
        val ping = Ping(session.topic)

        WalletConnectClient.ping(ping, object : SessionPing {
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

    override fun onSessionProposal(sessionProposal: SessionProposal) {
        viewModelScope.launch {
            this@WalletViewModel.proposal = sessionProposal
            _eventFlow.emit(ShowSessionProposalDialog(this@WalletViewModel.proposal))
        }
    }

    override fun onSessionRequest(sessionRequest: SessionRequest) {
        viewModelScope.launch {
            val session = WalletConnectClient.getListOfSettledSessions().find { session -> session.topic == sessionRequest.topic }!!
            _eventFlow.emit(ShowSessionRequestDialog(sessionRequest, session))
        }
    }

    override fun onSessionDelete(deletedSession: DeletedSession) {
        viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
    }

    override fun onSessionNotification(sessionNotification: SessionNotification) {
        //TODO handle session notification
    }
}