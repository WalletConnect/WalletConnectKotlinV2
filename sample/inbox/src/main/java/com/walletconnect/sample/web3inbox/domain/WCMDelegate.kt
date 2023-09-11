package com.walletconnect.sample.web3inbox.domain

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

object WCMDelegate : WalletConnectModal.ModalDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Modal.Model?> = MutableSharedFlow()
    val wcmEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

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
        Timber.d("onSessionRequestResponse($response)")
        scope.launch {
            Timber.d("onSessionRequestResponse($response)")
            _wcEventModels.emit(response)
        }
    }

    private fun deselectAccountDetails() {
        selectedSessionTopic = null
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        Timber.d("onConnectionStateChange($state)")
        scope.launch {
            _wcEventModels.emit(state)
        }
    }

    override fun onError(error: Modal.Model.Error) {
        Timber.d(error.throwable)
        scope.launch {
            _wcEventModels.emit(error)
        }
    }
}
