package com.walletconnect.wallet.domain

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object WalletDelegate : WalletConnectClient.WalletDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<WalletConnect.Model?> = MutableSharedFlow(1)
    val wcEventModels: SharedFlow<WalletConnect.Model?> = _wcEventModels

    var sessionProposal: WalletConnect.Model.SessionProposal? = null
        private set
    var selectedChainAddressId: Int = 1
        private set

    init {
        WalletConnectClient.setWalletDelegate(this)
    }

    override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
        this.sessionProposal = sessionProposal

        scope.launch {
            _wcEventModels.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        scope.launch {
            _wcEventModels.emit(sessionRequest)
        }
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {
        scope.launch {
            _wcEventModels.emit(sessionEvent)
        }
    }

    override fun onSessionSettleResponse(settleSessionResponse: WalletConnect.Model.SettledSessionResponse) {
        sessionProposal = null

        scope.launch {
            _wcEventModels.emit(settleSessionResponse)
        }
    }

    override fun onSessionUpdateAccountsResponse(sessionUpdateAccountsResponse: WalletConnect.Model.SessionUpdateAccountsResponse) {
        scope.launch {
            _wcEventModels.emit(sessionUpdateAccountsResponse)
        }
    }

    override fun onSessionUpdateMethodsResponse(sessionUpdateMethodsResponse: WalletConnect.Model.SessionUpdateMethodsResponse) {
        scope.launch {
            _wcEventModels.emit(sessionUpdateMethodsResponse)
        }
    }

    override fun onSessionUpdateEventsResponse(sessionUpdateEventsResponse: WalletConnect.Model.SessionUpdateEventsResponse) {
        scope.launch {
            _wcEventModels.emit(sessionUpdateEventsResponse)
        }
    }

    fun setSelectedAccount(selectedChainAddressId: Int) {
        this.selectedChainAddressId = selectedChainAddressId
    }

    fun clearCache() {
        _wcEventModels.resetReplayCache()
    }
}