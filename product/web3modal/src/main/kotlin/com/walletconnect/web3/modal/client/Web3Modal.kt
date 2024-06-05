package com.walletconnect.web3.modal.client

import androidx.activity.ComponentActivity
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.common.exceptions.SignClientAlreadyInitializedException
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.models.Account
import com.walletconnect.web3.modal.client.models.Session
import com.walletconnect.web3.modal.client.models.Web3ModelClientAlreadyInitializedException
import com.walletconnect.web3.modal.client.models.request.Request
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import com.walletconnect.web3.modal.di.web3ModalModule
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.domain.model.Session.WalletConnect
import com.walletconnect.web3.modal.domain.model.toModalError
import com.walletconnect.web3.modal.engine.Web3ModalEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.annotations.ApiStatus.Experimental
import org.koin.core.qualifier.named
import org.koin.dsl.module

object Web3Modal {

    internal var chains: List<Modal.Model.Chain> = listOf()

    internal var sessionProperties: Map<String, String>? = null

    internal var selectedChain: Modal.Model.Chain? = null

    private lateinit var web3ModalEngine: Web3ModalEngine

    interface ModalDelegate {
        fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession)

        @Deprecated(
            message = "Use onSessionEvent(Modal.Model.Event) instead. Using both will result in duplicate events.",
            replaceWith = ReplaceWith(expression = "onEvent(event)")
        )
        fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent)
        fun onSessionEvent(sessionEvent: Modal.Model.Event) {}
        fun onSessionExtend(session: Modal.Model.Session)
        fun onSessionDelete(deletedSession: Modal.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse)
        fun onSessionAuthenticateResponse(sessionUpdateResponse: Modal.Model.SessionAuthenticateResponse) {}

        // Utils
        fun onProposalExpired(proposal: Modal.Model.ExpiredProposal)
        fun onRequestExpired(request: Modal.Model.ExpiredRequest)
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
        onError: (Modal.Model.Error) -> Unit,
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

    @Experimental
    fun register(activity: ComponentActivity) {
        checkEngineInitialization()
        web3ModalEngine.registerCoinbaseLauncher(activity)
    }

    @Experimental
    fun unregister() {
        checkEngineInitialization()
        web3ModalEngine.unregisterCoinbase()
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::web3ModalEngine.isInitialized) {
            "Web3Modal needs to be initialized first using the initialize function"
        }
    }

    private fun onInitializedClient(
        init: Modal.Params.Init,
        onSuccess: () -> Unit = {},
        onError: (Modal.Model.Error) -> Unit,
    ) {
        if (!::web3ModalEngine.isInitialized) {
            runCatching {
                wcKoinApp.modules(web3ModalModule())
                web3ModalEngine = wcKoinApp.koin.get()
                web3ModalEngine.setup(init, onError)
                web3ModalEngine.setInternalDelegate(Web3ModalDelegate)
                wcKoinApp.modules(
                    module { single(named(AndroidCommonDITags.ENABLE_WEB_3_MODAL_ANALYTICS)) { init.enableAnalytics ?: web3ModalEngine.fetchAnalyticsConfig() } }
                )
            }
                .onFailure { error -> return@onInitializedClient onError(Modal.Model.Error(error)) }
                .onSuccess {
                    onSuccess()
                    web3ModalEngine.send(Props(event = EventType.TRACK, type = EventType.Track.MODAL_LOADED))
                }
        } else {
            onError(Modal.Model.Error(Web3ModelClientAlreadyInitializedException()))
        }
    }

    fun setChains(chains: List<Modal.Model.Chain>) {
        this.chains = chains
    }

    fun setSessionProperties(properties: Map<String, String>) {
        sessionProperties = properties
    }

    @Throws(IllegalStateException::class)
    fun setDelegate(delegate: ModalDelegate) {
        Web3ModalDelegate.connectionState.onEach { connectionState ->
            delegate.onConnectionStateChange(connectionState)
        }.launchIn(scope)

        Web3ModalDelegate.wcEventModels.onEach { event ->
            when (event) {
                is Modal.Model.ApprovedSession -> delegate.onSessionApproved(event)
                is Modal.Model.DeletedSession.Success -> delegate.onSessionDelete(event)
                is Modal.Model.Error -> delegate.onError(event)
                is Modal.Model.RejectedSession -> delegate.onSessionRejected(event)
                is Modal.Model.Session -> delegate.onSessionExtend(event)
                //todo: how to notify developer to not us both at the same time
                is Modal.Model.SessionEvent -> delegate.onSessionEvent(event)
                is Modal.Model.Event -> delegate.onSessionEvent(event)
                is Modal.Model.SessionRequestResponse -> delegate.onSessionRequestResponse(event)
                is Modal.Model.UpdatedSession -> delegate.onSessionUpdate(event)
                is Modal.Model.ExpiredRequest -> delegate.onRequestExpired(event)
                is Modal.Model.ExpiredProposal -> delegate.onProposalExpired(event)
                is Modal.Model.SessionAuthenticateResponse -> delegate.onSessionAuthenticateResponse(event)
                else -> Unit
            }
        }.launchIn(scope)
    }

    @Deprecated(
        message = "Replaced with the same name method but onSuccess callback returns a Pairing URL",
        replaceWith = ReplaceWith(expression = "fun connect(connect: Modal.Params.Connect, onSuccess: (String) -> Unit, onError: (Modal.Model.Error) -> Unit)")
    )
    internal fun connect(
        connect: Modal.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.connect(
            connect.toSign(),
            onSuccess
        ) { onError(it.toModal()) }
    }

    fun connect(
        connect: Modal.Params.Connect,
        onSuccess: (String) -> Unit,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.connect(
            connect = connect.toSign(),
            onSuccess = { url -> onSuccess(url) },
            onError = { onError(it.toModal()) }
        )
    }

    fun authenticate(
        authenticate: Modal.Params.Authenticate,
        onSuccess: (String) -> Unit,
        onError: (Modal.Model.Error) -> Unit,
    ) {

        SignClient.authenticate(authenticate.toSign(),
            onSuccess = { url -> onSuccess(url) },
            onError = { onError(it.toModal()) })
    }

    @Deprecated(
        message = "Modal.Params.Request is deprecated",
        replaceWith = ReplaceWith("com.walletconnect.web3.modal.client.models.Request")
    )
    fun request(
        request: Modal.Params.Request,
        onSuccess: (Modal.Model.SentRequest) -> Unit = {},
        onError: (Modal.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        web3ModalEngine.request(
            request = Request(request.method, request.params, request.expiry),
            onSuccess = { onSuccess(it.sentRequestToModal()) },
            onError = { onError(it.toModalError()) }
        )
    }

    fun request(
        request: Request,
        onSuccess: (SentRequestResult) -> Unit = {},
        onError: (Throwable) -> Unit,
    ) {
        checkEngineInitialization()
        web3ModalEngine.request(request, onSuccess, onError)
    }

    private fun SentRequestResult.sentRequestToModal() = when (this) {
        is SentRequestResult.Coinbase -> Modal.Model.SentRequest(Long.MIN_VALUE, String.Empty, method, params, chainId)
        is SentRequestResult.WalletConnect -> Modal.Model.SentRequest(requestId, sessionTopic, method, params, chainId)
    }

    fun request(
        request: Request,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        checkEngineInitialization()
        web3ModalEngine.request(request, { onSuccess() }, onError)
    }

    fun ping(sessionPing: Modal.Listeners.SessionPing? = null) = web3ModalEngine.ping(sessionPing)

    @Deprecated(
        message = "This has become deprecate in favor of the parameterless disconnect function",
        level = DeprecationLevel.WARNING
    )
    fun disconnect(
        onSuccess: (Modal.Params.Disconnect) -> Unit = {},
        onError: (Modal.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        val topic = when (val session = web3ModalEngine.getActiveSession()) {
            is WalletConnect -> session.topic
            else -> String.Empty
        }

        web3ModalEngine.disconnect(
            onSuccess = { onSuccess(Modal.Params.Disconnect(topic)) },
            onError = { onError(it.toModalError()) }
        )
    }

    fun disconnect(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        checkEngineInitialization()
        web3ModalEngine.disconnect(onSuccess, onError)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getSelectedChain() = selectedChain
//    fun getSelectedChain() = getSelectedChainUseCase()?.toChain()

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        message = "Getting active session is replaced with getAccount()",
        replaceWith = ReplaceWith("com.walletconnect.web3.modal.client.Web3Modal.getAccount()"),
        level = DeprecationLevel.WARNING
    )
    internal fun getActiveSessionByTopic(topic: String) = SignClient.getActiveSessionByTopic(topic)?.toModal()

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        message = "Getting active session is replaced with getAccount()",
        replaceWith = ReplaceWith("com.walletconnect.web3.modal.client.Web3Modal.getAccount()"),
        level = DeprecationLevel.WARNING
    )
    fun getActiveSession(): Modal.Model.Session? {
        checkEngineInitialization()
        return (web3ModalEngine.getActiveSession() as? WalletConnect)?.topic?.let { SignClient.getActiveSessionByTopic(it)?.toModal() }
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getAccount(): Account? {
        checkEngineInitialization()
        return web3ModalEngine.getAccount()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getSession(): Session? {
        checkEngineInitialization()
        return web3ModalEngine.getSession()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getConnectorType(): Modal.ConnectorType? {
        checkEngineInitialization()
        return web3ModalEngine.getConnectorType()
    }
}
