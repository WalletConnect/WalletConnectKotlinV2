package com.walletconnect.web3.modal.client

import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.web3.modal.di.web3ModalModule
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.domain.model.InvalidSessionException
import com.walletconnect.web3.modal.domain.model.toModalError
import com.walletconnect.web3.modal.utils.toChain
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Web3Modal {

    internal var excludedWalletsIds: List<String> = listOf()
    internal var recommendedWalletsIds: List<String> = listOf()

    internal var selectedChain: Modal.Model.Chain? = null
    internal var chains: List<Modal.Model.Chain> = listOf()

    internal var sessionProperties: Map<String, String>? = null

    interface ModalDelegate {
        fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession)
        fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent)
        fun onSessionExtend(session: Modal.Model.Session)
        fun onSessionDelete(deletedSession: Modal.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse)

        // Utils
        fun onConnectionStateChange(state: Modal.Model.ConnectionState)
        fun onError(error: Modal.Model.Error)
    }

    interface ComponentDelegate {
        fun onModalExpanded()

        fun onModalHidden()

    }

    fun initialize(
        init: Modal.Params.Init,
        onSuccess: () -> Unit = {},
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.initialize(
            init = Sign.Params.Init(init.core),
            onSuccess = {
                this.excludedWalletsIds = init.excludedWalletIds
                this.recommendedWalletsIds = init.recommendedWalletsIds
                runCatching {
                    wcKoinApp.modules(web3ModalModule())
                    setDelegate(Web3ModalDelegate)
                }.onFailure { error -> onError(Modal.Model.Error(error)) }
                onSuccess()
            },
            onError = { error ->
                onError(Modal.Model.Error(error.throwable))
                return@initialize
            }
        )
    }

    fun setChains(chains: List<Modal.Model.Chain>) {
        this.chains = chains
    }

    fun getSelectedChain() = Session.getSelectedChainId()?.toChain()

    internal fun getSelectedChainOrFirst() = selectedChain ?: chains.first()

    fun setSessionProperties(properties: Map<String, String>) {
        sessionProperties = properties
    }

    @Throws(IllegalStateException::class)
    fun setDelegate(delegate: ModalDelegate) {
        val signDelegate = object : SignClient.DappDelegate {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                delegate.onSessionApproved(approvedSession.toModal())
            }

            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                delegate.onSessionRejected(rejectedSession.toModal())
            }

            override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {
                delegate.onSessionUpdate(updatedSession.toModal())
            }

            override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {
                delegate.onSessionEvent(sessionEvent.toModal())
            }

            override fun onSessionExtend(session: Sign.Model.Session) {
                delegate.onSessionExtend(session.toModal())
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                delegate.onSessionDelete(deletedSession.toModal())
            }

            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                delegate.onSessionRequestResponse(response.toModal())
            }

            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
                delegate.onConnectionStateChange(state.toModal())
            }

            override fun onError(error: Sign.Model.Error) {
                delegate.onError(error.toModal())
            }
        }
        SignClient.setDappDelegate(signDelegate)
    }

    internal fun connect(
        connect: Modal.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.connect(
            connect.toSign(),
            onSuccess,
            { onError(it.toModal()) }
        )
    }

    fun request(request: Modal.Params.Request, onSuccess: (Modal.Model.SentRequest) -> Unit = {}, onError: (Modal.Model.Error) -> Unit) {
        val sessionTopic = Session.getSessionTopic()
        val selectedChainId = Session.getSelectedChainId()

        if (sessionTopic == null || selectedChainId == null) {
            onError(InvalidSessionException.toModalError())
            return
        }

        SignClient.request(
            request.toSign(sessionTopic, selectedChainId),
            { onSuccess(it.toModal()) },
            { onError(it.toModal()) }
        )
    }

    suspend fun request(request: Modal.Params.Request) = suspendCoroutine<Result<Modal.Model.SentRequest>> { continuation ->
        request(request, { continuation.resume(Result.success(it)) }, { continuation.resume(Result.failure(it.throwable)) })
    }

    fun ping(sessionPing: Modal.Listeners.SessionPing? = null) {
        val sessionTopic = Session.getSessionTopic()
        if (sessionTopic == null) {
            sessionPing?.onError(Modal.Model.Ping.Error(InvalidSessionException))
            return
        }
        SignClient.ping(Sign.Params.Ping(sessionTopic), sessionPing?.toSign())
    }

    fun disconnect(onSuccess: (Modal.Params.Disconnect) -> Unit = {}, onError: (Modal.Model.Error) -> Unit) {
        val sessionTopic = Session.getSessionTopic()

        if (sessionTopic == null) {
            onError(InvalidSessionException.toModalError())
            return
        }

        SignClient.disconnect(
            Sign.Params.Disconnect(sessionTopic),
            {
                Session.clearSessionData()
                onSuccess(it.toModal())
            },
            { onError(it.toModal()) }
        )
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    internal fun getActiveSessionByTopic(topic: String) = SignClient.getActiveSessionByTopic(topic)?.toModal()

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSession() = Session.getSessionTopic()?.let { SignClient.getActiveSessionByTopic(it)?.toModal() }
}
