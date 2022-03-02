package com.walletconnect.sample.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walletconnect.sample.wallet.ui.*
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class WalletViewModel : ViewModel(), WalletConnectClient.WalletDelegate {

    private var _eventFlow = MutableLiveData<Event<WalletUiEvent>>(Event(InitSessionsList(WalletConnectClient.getListOfSettledSessions())))
    val eventFlow: LiveData<Event<WalletUiEvent>> = _eventFlow

    private lateinit var proposal: WalletConnect.Model.SessionProposal

    init {
        WalletConnectClient.setWalletDelegate(this)
    }

    fun getPendingRequests(session: WalletConnect.Model.SettledSession) {
        val pendingRequests = WalletConnectClient.getPendingRequests(session.topic)
    }

    fun pair(uri: String) {
        val pair = WalletConnect.Params.Pair(uri.trim())
        WalletConnectClient.pair(pair)
    }

    fun approve() {
        val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
        val approve = WalletConnect.Params.Approve(proposal, accounts)
        WalletConnectClient.approve(approve) { error -> Log.d("Error", "sending approve error: $error") }
    }

    fun reject() {
        val rejectionReason = "Reject Session"
        val proposalTopic: String = proposal.topic
        val reject = WalletConnect.Params.Reject(rejectionReason, proposalTopic)
        WalletConnectClient.reject(reject) { error -> Log.d("Error", "sending reject error: $error") }
    }

    fun disconnect(topic: String) {
        val disconnect = WalletConnect.Params.Disconnect(
            sessionTopic = topic,
            reason = "User disconnects",
            reasonCode = 1000
        )
        WalletConnectClient.disconnect(disconnect)
        _eventFlow.value = Event(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions()))
    }

    fun respondRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val response = WalletConnect.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcResult(
                sessionRequest.request.id,
                "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
            )
        )

        WalletConnectClient.respond(response) { error -> Log.d("Error", "sending response error: $error") }
    }

    fun rejectRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val response = WalletConnect.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                WalletConnect.Model.JsonRpcResponse.Error(500, "Kotlin Wallet Error")
            )
        )

        WalletConnectClient.respond(response) { error -> Log.d("Error", "sending response error: $error") }
    }

    fun sessionUpdate(session: WalletConnect.Model.SettledSession) {
        val update = WalletConnect.Params.Update(
            sessionTopic = session.topic,
            sessionState = WalletConnect.Model.SessionState(accounts = listOf("eip155:42:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62")) //kovan
        )

        WalletConnectClient.update(update) { error -> Log.d("Error", "sending update error: $error") }
    }

    fun sessionUpgrade(session: WalletConnect.Model.SettledSession) {
        val permissions = WalletConnect.Model.SessionPermissions(
            blockchain = WalletConnect.Model.Blockchain(chains = listOf("eip155:80001")),
            jsonRpc = WalletConnect.Model.Jsonrpc(listOf("eth_sign"))
        )

        val upgrade = WalletConnect.Params.Upgrade(topic = session.topic, permissions = permissions)
        WalletConnectClient.upgrade(upgrade) { error -> Log.d("Error", "sending upgrade error: $error") }
    }

    fun sessionPing(session: WalletConnect.Model.SettledSession) {
        val ping = WalletConnect.Params.Ping(session.topic)
        WalletConnectClient.ping(ping, object : WalletConnect.Listeners.SessionPing {
            override fun onSuccess(topic: String) {
                Log.d("Ping", topic)
                _eventFlow.postValue(Event(Ping))
            }

            override fun onError(error: Throwable) {
                Log.d("Ping error", "Ping error")
            }
        })
    }

    override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
        this@WalletViewModel.proposal = sessionProposal
        _eventFlow.postValue(Event(ShowSessionProposalDialog(this@WalletViewModel.proposal)))
    }

    override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        val session = WalletConnectClient.getListOfSettledSessions().find { session -> session.topic == sessionRequest.topic }
        session?.let {
            _eventFlow.postValue(Event(ShowSessionRequestDialog(sessionRequest, it)))
        }
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        _eventFlow.postValue(Event(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())))
    }

    override fun onSessionNotification(sessionNotification: WalletConnect.Model.SessionNotification) {
        //session notification
    }

    override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
        //pairing settlement
    }

    override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
        when (response) {
            is WalletConnect.Model.SettledSessionResponse.Result -> {
                _eventFlow.postValue(Event(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())))
            }
            is WalletConnect.Model.SettledSessionResponse.Error -> Log.e("Error", "Settled session error: ${response.errorMessage}")
        }
    }

    override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {
        when (response) {
            is WalletConnect.Model.SessionUpgradeResponse.Result -> {
                Log.d("Session Upgrade", "Session upgrade result: $response")
                _eventFlow.postValue(Event(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())))
            }
            is WalletConnect.Model.SessionUpgradeResponse.Error -> Log.e("Error", "Session Upgrade error: ${response.errorMessage}")
        }
    }

    override fun onSessionUpdateResponse(response: WalletConnect.Model.SessionUpdateResponse) {
        when (response) {
            is WalletConnect.Model.SessionUpdateResponse.Result -> {
                Log.d("Session Update", "Session update result: $response")
                _eventFlow.postValue(Event(UpdateActiveSessions(WalletConnectClient.getListOfSettledSessions())))
            }
            is WalletConnect.Model.SessionUpdateResponse.Error -> Log.e("Error", "Session Update error: ${response.errorMessage}")
        }
    }
}