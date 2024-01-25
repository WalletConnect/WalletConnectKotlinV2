package com.walletconnect.web3.modal.domain.delegate

import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionTopicUseCase
import com.walletconnect.web3.modal.utils.EthUtils
import com.walletconnect.web3.modal.utils.getSelectedChain
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
    val wcEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()
    var sessionTopic: String? = null

    private val saveSessionTopicUseCase: SaveSessionTopicUseCase by lazy { wcKoinApp.koin.get() }
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase by lazy { wcKoinApp.koin.get() }
    private val getSelectedChainUseCase: GetSelectedChainUseCase by lazy { wcKoinApp.koin.get() }
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase by lazy { wcKoinApp.koin.get() }

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        //TODO That will be removed after coinbase integration to Web3ModalEngine
        scope.launch {
            saveSessionTopicUseCase(approvedSession.topic)
            saveChainSelectionUseCase(Web3Modal.chains.getSelectedChain(getSelectedChainUseCase()).id)
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
            saveSessionTopicUseCase(updatedSession.topic)
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        scope.launch {
            consumeSessionEvent(sessionEvent)
            _wcEventModels.emit(sessionEvent)
        }
    }

    private suspend fun consumeSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        try {
            when (sessionEvent.name) {
                EthUtils.accountsChanged -> {
                    val (_, chainReference, _) = sessionEvent.data.split(":")
                    Web3Modal.chains.find { it.chainReference == chainReference }?.let { chain -> saveChainSelectionUseCase(chain.id) }
                }
                EthUtils.chainChanged -> {
                    val (chainReference, _) = sessionEvent.data.split(".")
                    Web3Modal.chains.find { it.chainReference == chainReference }?.let { chain -> saveChainSelectionUseCase(chain.id) }
                }
            }
        } catch (throwable: Throwable) {
            onError(Modal.Model.Error(throwable))
        }
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        scope.launch {
            deleteSessionDataUseCase()
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

    override fun onSessionAuthenticateResponse(sessionUpdateResponse: Modal.Model.SessionAuthenticateResponse) {
        scope.launch {
            _wcEventModels.emit(sessionUpdateResponse)
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
