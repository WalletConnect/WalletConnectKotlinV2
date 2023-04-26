package com.walletconnect.sample.wallet.domain

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

object WCDelegate : Web3Wallet.WalletDelegate, CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
    val coreEvents: SharedFlow<Core.Model> = _coreEvents.asSharedFlow()

    private val _walletEvents: MutableSharedFlow<Wallet.Model> = MutableSharedFlow()
    val walletEvents: SharedFlow<Wallet.Model> = _walletEvents.asSharedFlow()
    var authRequest: Wallet.Model.AuthRequest? = null
    var sessionRequest: Wallet.Model.SessionRequest? = null

    init {
        CoreClient.setDelegate(this)
        Web3Wallet.setWalletDelegate(this)
    }

    override fun onAuthRequest(authRequest: Wallet.Model.AuthRequest) {
        this.authRequest = authRequest

        scope.launch {
            _walletEvents.emit(authRequest)
        }
    }

    override fun onConnectionStateChange(connectionStateChange: Wallet.Model.ConnectionState) {
        scope.launch {
            _walletEvents.emit(connectionStateChange)
        }
    }


    override fun onError(error: Wallet.Model.Error) {
        scope.launch {
            _walletEvents.emit(error)
        }
    }

    override fun onSessionDelete(deletedSession: Wallet.Model.SessionDelete) {
        scope.launch {
            _walletEvents.emit(deletedSession)
        }
    }

    override fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, sessionContext: Wallet.Model.SessionContext) {

        println("kobe: Proposal SessionContext: $sessionContext")

        scope.launch {
            _walletEvents.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, sessionContext: Wallet.Model.SessionContext) {
        this.sessionRequest = sessionRequest

        println("kobe: Request SessionContext: $sessionContext")

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