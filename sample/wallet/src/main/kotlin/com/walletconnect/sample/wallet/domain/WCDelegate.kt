package com.walletconnect.sample.wallet.domain

import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

object WCDelegate : Web3Wallet.WalletDelegate, CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
    val coreEvents: SharedFlow<Core.Model> = _coreEvents.asSharedFlow()

    private val _walletEvents: MutableSharedFlow<Wallet.Model> = MutableSharedFlow()
    val walletEvents: SharedFlow<Wallet.Model> = _walletEvents.asSharedFlow()
    var authRequestEvent: Pair<Wallet.Model.AuthRequest, Wallet.Model.VerifyContext>? = null
    var sessionProposalEvent: Pair<Wallet.Model.SessionProposal, Wallet.Model.VerifyContext>? = null
    var sessionRequestEvent: Pair<Wallet.Model.SessionRequest, Wallet.Model.VerifyContext>? = null

    init {
        CoreClient.setDelegate(this)
        Web3Wallet.setWalletDelegate(this)
    }

    override fun onAuthRequest(authRequest: Wallet.Model.AuthRequest, verifyContext: Wallet.Model.VerifyContext) {
        authRequestEvent = Pair(authRequest, verifyContext)

        scope.launch {
            _walletEvents.emit(authRequest)
        }
    }

    override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
        scope.launch {
            _walletEvents.emit(state)
        }
    }


    override fun onError(error: Wallet.Model.Error) {
        mixPanel.track("error", JSONObject().put("error", error.throwable.stackTraceToString()))
        scope.launch {
            _walletEvents.emit(error)
        }
    }

    override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
        scope.launch {
            _walletEvents.emit(sessionDelete)
        }
    }

    override fun onSessionExtend(session: Wallet.Model.Session) {
        Log.d("Session Extend", "${session.expiry}")
    }

    override fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, verifyContext: Wallet.Model.VerifyContext) {
        sessionProposalEvent = Pair(sessionProposal, verifyContext)

        scope.launch {
            _walletEvents.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, verifyContext: Wallet.Model.VerifyContext) {
        sessionRequestEvent = Pair(sessionRequest, verifyContext)

        scope.launch {
            _walletEvents.emit(sessionRequest)
        }
    }

    override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
        scope.launch {
            _walletEvents.emit(settleSessionResponse)
        }
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
        scope.launch {
            _walletEvents.emit(sessionUpdateResponse)
        }
    }

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        scope.launch {
            _coreEvents.emit(deletedPairing)
        }
    }
}