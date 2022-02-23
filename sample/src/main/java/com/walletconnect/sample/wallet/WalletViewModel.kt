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
        WalletConnectClient.pair(pair) { error -> Log.d("kobe", "sending pair error: $error") }
    }

    fun approve() {
        val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approve = WalletConnect.Params.Approve(proposal, accounts)
        WalletConnectClient.approve(approve) { error -> Log.d("kobe", "sending approve error: $error") }
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = proposal.topic
        val reject = WalletConnect.Params.Reject(rejectionReason, proposalTopic)

        WalletConnectClient.reject(reject) { error -> Log.d("kobe", "sending reject error: $error") }
        viewModelScope.launch { _eventFlow.emit(RejectSession) }
    }

    fun disconnect(topic: String) {
        val disconnect = WalletConnect.Params.Disconnect(
            sessionTopic = topic,
            reason = "User disconnects",
            reasonCode = 1000
        )

        WalletConnectClient.disconnect(disconnect) { error -> Log.d("kobe", "sending disconnect error: $error") }
        viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
    }

    fun respondRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val response = WalletConnect.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcResult(
                sessionRequest.request.id,
                "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
            )
        )

        WalletConnectClient.respond(response) { error -> Log.d("kobe", "sending response error: $error") }
    }

    fun rejectRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val response = WalletConnect.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                WalletConnect.Model.JsonRpcResponse.Error(500, "Kotlin Wallet Error")
            )
        )

        WalletConnectClient.respond(response) { error -> Log.d("kobe", "sending response error: $error") }
    }

    fun sessionUpdate(session: WalletConnect.Model.SettledSession) {
        val update = WalletConnect.Params.Update(
            sessionTopic = session.topic,
            sessionState = WalletConnect.Model.SessionState(accounts = listOf("${proposal.chains[0]}:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62"))
        )

        WalletConnectClient.update(update) { error -> Log.d("kobe", "sending update error: $error") }
    }

    fun sessionUpgrade(session: WalletConnect.Model.SettledSession) {
        val permissions = WalletConnect.Model.SessionPermissions(
            blockchain = WalletConnect.Model.Blockchain(chains = listOf("eip155:80001")),
            jsonRpc = WalletConnect.Model.Jsonrpc(listOf("eth_sign"))
        )

        val upgrade = WalletConnect.Params.Upgrade(topic = session.topic, permissions = permissions)

        WalletConnectClient.upgrade(upgrade) { error -> Log.d("kobe", "sending upgrade error: $error") }
    }

    fun sessionPing(session: WalletConnect.Model.SettledSession) {
        val ping = WalletConnect.Params.Ping(session.topic)

        WalletConnectClient.ping(ping, object : WalletConnect.Listeners.SessionPing {
            override fun onSuccess(topic: String) {
                Log.d("kobe", "ping: $topic")
            }

            override fun onError(error: Throwable) {
                Log.d("kobe", "Ping error")
            }
        })
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

    override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
        when (response) {
            is WalletConnect.Model.SettledPairingResponse.Result -> Log.e("kobe", "Pairing settlement success: ${response.topic}")
            is WalletConnect.Model.SettledPairingResponse.Error -> Log.e("kobe", "Pairing settlement error: ${response.errorMessage}")
        }
    }

    override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
        when (response) {
            is WalletConnect.Model.SettledSessionResponse.Result -> {
                viewModelScope.launch { _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())) }
            }
            is WalletConnect.Model.SettledSessionResponse.Error -> Log.e("kobe", "Settled session error: ${response.errorMessage}")
        }
    }

    override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {
        when (response) {
            is WalletConnect.Model.SessionUpgradeResponse.Result -> {
                viewModelScope.launch {
                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session upgrade"))
                }
            }
            is WalletConnect.Model.SessionUpgradeResponse.Error -> Log.e("kobe", "Session Upgrade error: ${response.errorMessage}")
        }
    }

    override fun onSessionUpdateResponse(response: WalletConnect.Model.SessionUpdateResponse) {
        when (response) {
            is WalletConnect.Model.SessionUpdateResponse.Result -> {
                viewModelScope.launch {
                    _eventFlow.emit(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions(), "Successful session update"))
                }
            }
            is WalletConnect.Model.SessionUpdateResponse.Error -> Log.e("kobe", "Session Update error: ${response.errorMessage}")
        }
    }
}