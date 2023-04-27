package com.walletconnect.wallet.domain

import android.util.Log
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object WalletDelegate : SignClient.WalletDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Sign.Model?> = MutableSharedFlow(1)
    val wcEventModels: SharedFlow<Sign.Model?> = _wcEventModels

    var selectedChainAddressId: Int = 1
        private set

    init {
        SignClient.setWalletDelegate(this)
    }

    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
        scope.launch {
            _wcEventModels.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {
        scope.launch {
            _wcEventModels.emit(sessionRequest)
        }
    }

    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
        scope.launch {
            _wcEventModels.emit(settleSessionResponse)
        }
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
        scope.launch {
            _wcEventModels.emit(sessionUpdateResponse)
        }
    }

    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Log.d(tag(this), "onConnectionStateChange($state)")
    }

    override fun onError(error: Sign.Model.Error) {
        Log.e(tag(this), error.throwable.stackTraceToString())
    }

    fun setSelectedAccount(selectedChainAddressId: Int) {
        WalletDelegate.selectedChainAddressId = selectedChainAddressId
    }

    fun clearCache() {
        _wcEventModels.resetReplayCache()
    }
}