package com.walletconnect.sample.dapp.domain

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sample.common.tag
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

object DappDelegate : WalletConnectModal.ModalDelegate, CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Modal.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()

    private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
    val coreEvents: SharedFlow<Core.Model> = _coreEvents.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

    init {
        WalletConnectModal.setDelegate(this)
        CoreClient.setDelegate(this)
    }

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        selectedSessionTopic = approvedSession.topic

        scope.launch {
            _wcEventModels.emit(approvedSession)
        }
    }

    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        scope.launch {
            _wcEventModels.emit(rejectedSession)
        }
    }

    override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
        scope.launch {
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        scope.launch {
            _wcEventModels.emit(sessionEvent)
        }
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        deselectAccountDetails()

        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionExtend(session: Modal.Model.Session) {
        scope.launch {
            _wcEventModels.emit(session)
        }
    }

    override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    override fun onSessionAuthenticateResponse(sessionUpdateResponse: Modal.Model.SessionAuthenticateResponse) {
        if (sessionUpdateResponse is Modal.Model.SessionAuthenticateResponse.Result) {
            selectedSessionTopic = sessionUpdateResponse.session?.topic
        }
        scope.launch {
            _wcEventModels.emit(sessionUpdateResponse)
        }
    }

    override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
        scope.launch {
            _wcEventModels.emit(proposal)
        }
    }

    override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
        scope.launch {
            _wcEventModels.emit(request)
        }
    }

    fun deselectAccountDetails() {
        selectedSessionTopic = null
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        Timber.d(tag(this), "onConnectionStateChange($state)")
        scope.launch {
            _wcEventModels.emit(state)
        }
    }

    override fun onError(error: Modal.Model.Error) {
        Timber.d(tag(this), error.throwable.stackTraceToString())
        scope.launch {
            _wcEventModels.emit(error)
        }
    }

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        Timber.d(tag(this), "Pairing deleted: ${deletedPairing.topic}")
    }

    override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
        scope.launch {
            _coreEvents.emit(expiredPairing)
        }
    }

    override fun onPairingState(pairingState: Core.Model.PairingState) {
        Timber.d(tag(this), "Dapp pairing state: ${pairingState.isPairingState}")
    }
}
