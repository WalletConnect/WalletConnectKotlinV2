package com.walletconnect.wallet.domain

import android.util.Log
import com.walletconnect.sample_common.tag

import com.walletconnect.wallet.client.Wallet
import com.walletconnect.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object WalletDelegate : Web3Wallet.WalletDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Wallet.Model?> = MutableSharedFlow(1)
    val wcEventModels: SharedFlow<Wallet.Model?> = _wcEventModels

    var sessionProposal: Wallet.Model.SessionProposal? = null
        private set
    var selectedChainAddressId: Int = 1
        private set

    init {
        Web3Wallet.setWalletDelegate(this)
    }

    override fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal) {
        WalletDelegate.sessionProposal = sessionProposal

        scope.launch {
            _wcEventModels.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest) {
        scope.launch {
            _wcEventModels.emit(sessionRequest)
        }
    }

    override fun onSessionDelete(deletedSession: Wallet.Model.SessionDelete) {
        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onAuthRequest(authRequest: Wallet.Model.AuthRequest) {
        TODO("Not yet implemented")
    }

    override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
        sessionProposal = null

        scope.launch {
            _wcEventModels.emit(settleSessionResponse)
        }
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
        scope.launch {
            _wcEventModels.emit(sessionUpdateResponse)
        }
    }

    override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
        Log.d(tag(this), "onConnectionStateChange($state)")
    }

    override fun onError(error: Wallet.Model.Error) {
        Log.e(tag(this), error.throwable.stackTraceToString())
    }

    fun setSelectedAccount(selectedChainAddressId: Int) {
        WalletDelegate.selectedChainAddressId = selectedChainAddressId
    }

    fun clearCache() {
        _wcEventModels.resetReplayCache()
    }
}