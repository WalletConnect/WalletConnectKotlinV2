package com.walletconnect.sample.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.wallet.ui.*
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel(), WalletConnectClient.WalletDelegate {

    private var _eventFlow = MutableStateFlow<WalletUiEvent>(InitSessionsList(WalletConnectClient.getListOfSettledSessions()))
    val eventFlow: LiveData<WalletUiEvent> = _eventFlow.asLiveData()

    private lateinit var proposal: WalletConnect.Model.SessionProposal

    init {
        WalletConnectClient.setWalletDelegate(this)
    }

    fun pair(uri: String) {
        val pair = WalletConnect.Params.Pair(uri.trim())
        WalletConnectClient.pair(pair, object : WalletConnect.Listeners.Pairing {
            override fun onSuccess(settledPairing: WalletConnect.Model.SettledPairing) {
                Log.d("kobe", "settledPairing: $settledPairing")

                //Settled pairing
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "settledSession error: $error")
                //Pairing approval error
            }
        })
    }

    fun approve() {
        val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approve = WalletConnect.Params.Approve(proposal, accounts)

        WalletConnectClient.approve(approve, object : WalletConnect.Listeners.SessionApprove {

            override fun onSuccess(settledSession: WalletConnect.Model.SettledSession) {

                Log.d("kobe", "settledSession: $settledSession")

                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "settledSession: $error")
                //Approve session error
            }
        })
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = proposal.topic
        val reject = WalletConnect.Params.Reject(rejectionReason, proposalTopic)

        WalletConnectClient.reject(reject, object : WalletConnect.Listeners.SessionReject {
            override fun onSuccess(rejectedSession: WalletConnect.Model.RejectedSession) {

                Log.d("kobe", "rejectedSession: $rejectedSession")

                viewModelScope.launch { _eventFlow.emit(RejectSession) }
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "reject: $error")
                //Reject proposal error
            }
        })
    }

    fun disconnect(topic: String) {
        val disconnect = WalletConnect.Params.Disconnect(
            sessionTopic = topic,
            reason = "User disconnects",
            reasonCode = 1000
        )

        WalletConnectClient.disconnect(disconnect, object : WalletConnect.Listeners.SessionDelete {
            override fun onSuccess(deletedSession: WalletConnect.Model.DeletedSession) {
                Log.d("kobe", "deletedSession: $deletedSession")
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "disconnect: $error")
                //Session disconnect error
            }
        })
    }

    fun respondRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val response = WalletConnect.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcResult(
                sessionRequest.request.id,
                "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
            )
        )

        WalletConnectClient.respond(response, object : WalletConnect.Listeners.SessionPayload {
            override fun onError(error: Throwable) {
                Log.d("kobe", "respondRequest: $error")
                //Error
            }
        })
    }

    fun rejectRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val response = WalletConnect.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                WalletConnect.Model.JsonRpcResponse.Error(500, "Kotlin Wallet Error")
            )
        )

        WalletConnectClient.respond(response, object : WalletConnect.Listeners.SessionPayload {
            override fun onError(error: Throwable) {
                Log.d("kobe", "rejectRequest: $error")
                //Error
            }
        })
    }

    fun sessionUpdate(session: WalletConnect.Model.SettledSession) {
        val update = WalletConnect.Params.Update(
            sessionTopic = session.topic,
            sessionState = WalletConnect.Model.SessionState(accounts = listOf("${proposal.chains[0]}:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62"))
        )

        WalletConnectClient.update(update, object : WalletConnect.Listeners.SessionUpdate {
            override fun onSuccess(updatedSession: WalletConnect.Model.UpdatedSession) {

//                Log.d("kobe", "updatedSession: $updatedSession")
//                viewModelScope.launch {
//                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session update"))
//                }
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "update: $error")
                //Error
            }
        })
    }

    fun sessionUpgrade(session: WalletConnect.Model.SettledSession) {
        val permissions =
            WalletConnect.Model.SessionPermissions(
                blockchain = WalletConnect.Model.Blockchain(chains = listOf("eip155:80001")),
                jsonRpc = WalletConnect.Model.Jsonrpc(listOf("eth_sign"))
            )
        val upgrade = WalletConnect.Params.Upgrade(topic = session.topic, permissions = permissions)

        WalletConnectClient.upgrade(upgrade, object : WalletConnect.Listeners.SessionUpgrade {
            override fun onSuccess(upgradedSession: WalletConnect.Model.UpgradedSession) {

//                Log.d("kobe", "upgradedSession: $upgradedSession")
//                viewModelScope.launch {
//                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session upgrade"))
//                }
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "upgrade: $error")
                //Error
            }
        })
    }

    fun sessionPing(session: WalletConnect.Model.SettledSession) {
        val ping = WalletConnect.Params.Ping(session.topic)

        WalletConnectClient.ping(ping, object : WalletConnect.Listeners.SessionPing {
            override fun onSuccess(topic: String) {

                Log.d("kobe", "ping: $topic")

                viewModelScope.launch {
                    _eventFlow.emit(PingSuccess)
                }
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "Ping error")
                //Error
            }
        })
    }

    override fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing) {
        Log.d("kobe", "settledPairing: $settledPairing")
    }

    override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
        viewModelScope.launch {
            this@WalletViewModel.proposal = sessionProposal
            _eventFlow.emit(ShowSessionProposalDialog(this@WalletViewModel.proposal))
        }
    }

    override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        viewModelScope.launch {
            val session = WalletConnectClient.getListOfSettledSessions().find { session -> session.topic == sessionRequest.topic }!!
            _eventFlow.emit(ShowSessionRequestDialog(sessionRequest, session))
        }
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
    }

    override fun onSessionNotification(sessionNotification: WalletConnect.Model.SessionNotification) {
        //TODO handle session notification
    }

    override fun onSessionUpgraded(upgradedSession: WalletConnect.Model.UpgradedSession) {
        Log.d("kobe", "upgradedSession: $upgradedSession")

        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session upgrade"))
        }
    }

    override fun onSessionUpdated(updatedSession: WalletConnect.Model.UpdatedSession) {
        Log.d("kobe", "updatedSession: $updatedSession")

        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session update"))
        }
    }

    override fun onSessionSettled(settledSession: WalletConnect.Model.SettledSession) {
        Log.d("kobe", "settledSession: $settledSession")

        viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
    }
}