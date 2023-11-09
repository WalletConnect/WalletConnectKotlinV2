package com.walletconnect.web3.modal.domain.delegate

import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal object Web3ModalDelegate : Web3Modal.ModalDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Modal.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<Modal.Model?> =  _wcEventModels.asSharedFlow()

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
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

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        scope.launch {
            _wcEventModels.emit(state)
        }
    }

    override fun onError(error: Modal.Model.Error) {
        scope.launch {
            _wcEventModels.emit(error)
        }
    }
}
