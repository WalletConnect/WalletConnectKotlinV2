package com.walletconnect.dapp.domain

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object DappDelegate : WalletConnectClient.DappDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<WalletConnect.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<WalletConnect.Model?> = _wcEventModels.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

    init {
        WalletConnectClient.setDappDelegate(this)
    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
        selectedSessionTopic = approvedSession.topic

        scope.launch {
            _wcEventModels.emit(approvedSession)
        }
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
        scope.launch {
            _wcEventModels.emit(rejectedSession)
        }
    }

    // TODO: Remove
    override fun onSessionUpdateAccounts(updatedSessionAccounts: WalletConnect.Model.UpdatedSessionAccounts) {
        scope.launch {
            _wcEventModels.emit(updatedSessionAccounts)
        }
    }

    override fun onSessionUpdateNamespaces(updatedSessionNamespaces: WalletConnect.Model.UpdateSessionNamespaces) {
        scope.launch {
            _wcEventModels.emit(updatedSessionNamespaces)
        }
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        deselectAccountDetails()

        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onUpdateSessionExpiry(session: WalletConnect.Model.Session) {
        scope.launch {
            _wcEventModels.emit(session)
        }
    }

    override fun onSessionRequestResponse(response: WalletConnect.Model.SessionRequestResponse) {
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    fun deselectAccountDetails() {
        selectedSessionTopic = null
    }
}