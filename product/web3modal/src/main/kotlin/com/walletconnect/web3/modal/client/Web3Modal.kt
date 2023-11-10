package com.walletconnect.web3.modal.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.common.exceptions.SignClientAlreadyInitializedException
import com.walletconnect.web3.modal.di.web3ModalModule
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.domain.model.InvalidSessionException
import com.walletconnect.web3.modal.domain.model.toModalError
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.utils.toChain
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO Change to Client/Protocol/Engine Pattern to break up public facing functions with business logic
object Web3Modal {

    internal var excludedWalletsIds: List<String> = listOf()
    internal var recommendedWalletsIds: List<String> = listOf()

    internal var chains: List<Modal.Model.Chain> = listOf()

    internal var sessionProperties: Map<String, String>? = null

    private val getSessionTopicUseCase: GetSessionTopicUseCase by lazy { wcKoinApp.koin.get() }
    private val getSelectedChainUseCase: GetSelectedChainUseCase by lazy { wcKoinApp.koin.get() }
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase by lazy { wcKoinApp.koin.get() }

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
                onInitializedClient(init, onSuccess, onError)
            },
            onError = { error ->
                if (error.throwable is SignClientAlreadyInitializedException) {
                    onInitializedClient(init, onSuccess, onError)
                } else {
                    return@initialize onError(Modal.Model.Error(error.throwable))
                }
            }
        )
    }

    private fun onInitializedClient(
        init: Modal.Params.Init,
        onSuccess: () -> Unit = {},
        onError: (Modal.Model.Error) -> Unit
    ) {
        this.excludedWalletsIds = init.excludedWalletIds
        this.recommendedWalletsIds = init.recommendedWalletsIds
        runCatching {
            wcKoinApp.modules(web3ModalModule())
            setInternalDelegate(Web3ModalDelegate)
        }.onFailure { error -> return@onInitializedClient onError(Modal.Model.Error(error)) }
        onSuccess()
    }

    fun setChains(chains: List<Modal.Model.Chain>) {
        this.chains = chains
    }

    fun getSelectedChain() = getSelectedChainUseCase()?.toChain()

    internal fun getSelectedChainOrFirst() = getSelectedChain() ?: chains.first()

    fun setSessionProperties(properties: Map<String, String>) {
        sessionProperties = properties
    }

    @Throws(IllegalStateException::class)
    fun setDelegate(delegate: ModalDelegate) {
        Web3ModalDelegate.wcEventModels.onEach { event ->
            when(event) {
                is Modal.Model.ApprovedSession -> delegate.onSessionApproved(event)
                is Modal.Model.ConnectionState -> delegate.onConnectionStateChange(event)
                is Modal.Model.DeletedSession.Success -> delegate.onSessionDelete(event)
                is Modal.Model.Error -> delegate.onError(event)
                is Modal.Model.RejectedSession -> delegate.onSessionRejected(event)
                is Modal.Model.Session -> delegate.onSessionExtend(event)
                is Modal.Model.SessionEvent -> delegate.onSessionEvent(event)
                is Modal.Model.SessionRequestResponse -> delegate.onSessionRequestResponse(event)
                is Modal.Model.UpdatedSession -> delegate.onSessionUpdate(event)
                else -> Unit
            }
        }.launchIn(scope)
    }

    @Throws(IllegalStateException::class)
    private fun setInternalDelegate(delegate: ModalDelegate) {
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
        val sessionTopic = getSessionTopicUseCase()
        val selectedChainId = getSelectedChainUseCase()

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
        val sessionTopic = getSessionTopicUseCase()
        if (sessionTopic == null) {
            sessionPing?.onError(Modal.Model.Ping.Error(InvalidSessionException))
            return
        }
        SignClient.ping(Sign.Params.Ping(sessionTopic), sessionPing?.toSign())
    }

    fun disconnect(onSuccess: (Modal.Params.Disconnect) -> Unit = {}, onError: (Modal.Model.Error) -> Unit) {
        val sessionTopic = getSessionTopicUseCase()

        if (sessionTopic == null) {
            onError(InvalidSessionException.toModalError())
            return
        }

        SignClient.disconnect(
            Sign.Params.Disconnect(sessionTopic),
            {
                scope.launch { deleteSessionDataUseCase() }
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
    fun getActiveSession() = getSessionTopicUseCase()?.let { SignClient.getActiveSessionByTopic(it)?.toModal() }
}
