package com.walletconnect.dapp.domain

import android.util.Log
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger

object DappDelegate : WalletConnectClient.DappDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<WalletConnect.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<WalletConnect.Model?> =  _wcEventModels.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

    init {
        WalletConnectClient.setDappDelegate(this){ error ->
            Log.e(tag(this), error.error.stackTraceToString())
        }
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

    override fun onSessionUpdateAccounts(updatedSessionAccounts: WalletConnect.Model.UpdatedSessionAccounts) {
        scope.launch {
            _wcEventModels.emit(updatedSessionAccounts)
        }
    }

    override fun onSessionUpdateMethods(updatedSessionMethods: WalletConnect.Model.UpdatedSessionMethods) {
        scope.launch {
            _wcEventModels.emit(updatedSessionMethods)
        }
    }

    override fun onSessionUpdateEvents(updatedSessionEvents: WalletConnect.Model.UpdatedSessionEvents) {
        scope.launch {
            _wcEventModels.emit(updatedSessionEvents)
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

    override fun onNetworkStateChange(state: WalletConnect.Model.NetworkState) {
        Log.d(tag(this), "onNetworkStateChange($state)")
    }
}