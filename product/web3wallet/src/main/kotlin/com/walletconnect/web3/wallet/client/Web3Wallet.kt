package com.walletconnect.web3.wallet.client

import com.walletconnect.android.Core
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.auth.common.exceptions.AuthClientAlreadyInitializedException
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.common.exceptions.SignClientAlreadyInitializedException
import kotlinx.coroutines.*
import java.util.*

@Deprecated("Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin")
object Web3Wallet {
    private lateinit var coreClient: CoreInterface

    @Deprecated("Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin", replaceWith = ReplaceWith("WalletKit.WalletDelegate"))
    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, verifyContext: Wallet.Model.VerifyContext)
        val onSessionAuthenticate: ((Wallet.Model.SessionAuthenticate, Wallet.Model.VerifyContext) -> Unit)? get() = null
        fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, verifyContext: Wallet.Model.VerifyContext)
        fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete)
        fun onSessionExtend(session: Wallet.Model.Session)

        @Deprecated(
            "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
            replaceWith = ReplaceWith("fun onSessionAuthenticated(sessionAuthenticate: Wallet.Model.SessionAuthenticate, verifyContext: Wallet.Model.VerifyContext)")
        )
        fun onAuthRequest(authRequest: Wallet.Model.AuthRequest, verifyContext: Wallet.Model.VerifyContext)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse)

        //Utils
        fun onProposalExpired(proposal: Wallet.Model.ExpiredProposal) {
            //override me
        }

        fun onRequestExpired(request: Wallet.Model.ExpiredRequest) {
            //override me
        }

        fun onConnectionStateChange(state: Wallet.Model.ConnectionState)
        fun onError(error: Wallet.Model.Error)
    }

    @Deprecated("Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin", replaceWith = ReplaceWith("WalletKit.setWalletDelegate(delegate)"))
    @Throws(IllegalStateException::class)
    fun setWalletDelegate(delegate: WalletDelegate) {
        val isSessionAuthenticateImplemented = delegate.onSessionAuthenticate != null

        val signWalletDelegate = object : SignClient.WalletDelegate {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                delegate.onSessionProposal(sessionProposal.toWallet(), verifyContext.toWallet())
            }

            override val onSessionAuthenticate: ((Sign.Model.SessionAuthenticate, Sign.Model.VerifyContext) -> Unit)?
                get() = if (isSessionAuthenticateImplemented) {
                    { sessionAuthenticate, verifyContext ->
                        delegate.onSessionAuthenticate?.invoke(sessionAuthenticate.toWallet(), verifyContext.toWallet())
                    }
                } else {
                    null
                }

            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {
                delegate.onSessionRequest(sessionRequest.toWallet(), verifyContext.toWallet())
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                delegate.onSessionDelete(deletedSession.toWallet())
            }

            override fun onSessionExtend(session: Sign.Model.Session) {
                delegate.onSessionExtend(session.toWallet())
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                delegate.onSessionSettleResponse(settleSessionResponse.toWallet())
            }

            override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
                delegate.onSessionUpdateResponse(sessionUpdateResponse.toWallet())
            }

            override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {
                delegate.onProposalExpired(proposal.toWallet())
            }

            override fun onRequestExpired(request: Sign.Model.ExpiredRequest) {
                delegate.onRequestExpired(request.toWallet())
            }

            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
                delegate.onConnectionStateChange(Wallet.Model.ConnectionState(state.isAvailable, state.reason?.toWallet()))
            }

            override fun onError(error: Sign.Model.Error) {
                delegate.onError(Wallet.Model.Error(error.throwable))
            }
        }

        val authWalletDelegate = object : AuthClient.ResponderDelegate {
            override fun onAuthRequest(authRequest: Auth.Event.AuthRequest, verifyContext: Auth.Event.VerifyContext) {
                delegate.onAuthRequest(authRequest.toWallet(), verifyContext.toWallet())
            }

            override fun onConnectionStateChange(connectionStateChange: Auth.Event.ConnectionStateChange) {
                //ignore
            }

            override fun onError(error: Auth.Event.Error) {
                delegate.onError(Wallet.Model.Error(error.error.throwable))
            }
        }

        SignClient.setWalletDelegate(signWalletDelegate)
        //TODO: Remove AuthClient setting responder delegate in the future
        AuthClient.setResponderDelegate(authWalletDelegate)
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.initialize(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun initialize(params: Wallet.Params.Init, onSuccess: () -> Unit = {}, onError: (Wallet.Model.Error) -> Unit) {
        coreClient = params.core
        var clientInitCounter = 0
        val onSuccessfulInitialization: () -> Unit = { clientInitCounter++ }

        SignClient.initialize(Sign.Params.Init(params.core), onSuccess = onSuccessfulInitialization) { error ->
            if (error.throwable is SignClientAlreadyInitializedException) {
                onSuccessfulInitialization()
            } else {
                onError(Wallet.Model.Error(error.throwable))
            }
        }
        //TODO: Remove AuthClient initialization in the future
        AuthClient.initialize(Auth.Params.Init(params.core), onSuccess = onSuccessfulInitialization) { error ->
            if (error.throwable is AuthClientAlreadyInitializedException) {
                onSuccessfulInitialization()
            } else {
                onError(Wallet.Model.Error(error.throwable))
            }
        }
        validateInitializationCount(clientInitCounter, onSuccess, onError)
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.registerDeviceToken(firebaseAccessToken, enableEncrypted, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun registerDeviceToken(firebaseAccessToken: String, enableEncrypted: Boolean = false, onSuccess: () -> Unit, onError: (Wallet.Model.Error) -> Unit) {
        coreClient.Echo.register(firebaseAccessToken, enableEncrypted, onSuccess) { error -> onError(Wallet.Model.Error(error)) }
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.decryptMessage(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun decryptMessage(params: Wallet.Params.DecryptMessage, onSuccess: (Wallet.Model.Message) -> Unit, onError: (Wallet.Model.Error) -> Unit) {
        scope.launch {
            SignClient.decryptMessage(
                Sign.Params.DecryptMessage(params.topic, params.encryptedMessage),
                onSuccess = { message ->
                    when (message) {
                        is Sign.Model.Message.SessionRequest -> onSuccess(message.toWallet())
                        is Sign.Model.Message.SessionProposal -> onSuccess(message.toWallet())
                        else -> { /*Ignore*/
                        }
                    }
                },
                onError = { signError -> onError(Wallet.Model.Error(signError.throwable)) })
        }
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.dispatchEnvelope(urlWithEnvelope, onError)")
    )
    @Throws(IllegalStateException::class)
    fun dispatchEnvelope(urlWithEnvelope: String, onError: (Wallet.Model.Error) -> Unit) {
        scope.launch {
            try {
                SignClient.dispatchEnvelope(urlWithEnvelope) { error -> onError(Wallet.Model.Error(error.throwable)) }
            } catch (error: Exception) {
                onError(Wallet.Model.Error(error))
            }
        }
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.pair(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun pair(params: Wallet.Params.Pair, onSuccess: (Wallet.Params.Pair) -> Unit = {}, onError: (Wallet.Model.Error) -> Unit = {}) {
        coreClient.Pairing.pair(Core.Params.Pair(params.uri), { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.approveSession(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun approveSession(
        params: Wallet.Params.SessionApprove,
        onSuccess: (Wallet.Params.SessionApprove) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Approve(params.proposerPublicKey, params.namespaces.toSign(), params.relayProtocol)
        SignClient.approveSession(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.generateApprovedNamespaces(sessionProposal, supportedNamespaces, onError)")
    )
    @Throws(Exception::class)
    fun generateApprovedNamespaces(sessionProposal: Wallet.Model.SessionProposal, supportedNamespaces: Map<String, Wallet.Model.Namespace.Session>): Map<String, Wallet.Model.Namespace.Session> {
        return com.walletconnect.sign.client.utils.generateApprovedNamespaces(sessionProposal.toSign(), supportedNamespaces.toSign()).toWallet()
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.rejectSession(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun rejectSession(
        params: Wallet.Params.SessionReject,
        onSuccess: (Wallet.Params.SessionReject) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Reject(params.proposerPublicKey, params.reason)
        SignClient.rejectSession(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.approveSessionAuthenticate(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun approveSessionAuthenticate(
        params: Wallet.Params.ApproveSessionAuthenticate,
        onSuccess: (Wallet.Params.ApproveSessionAuthenticate) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.ApproveAuthenticate(params.id, params.auths.toSign())
        SignClient.approveAuthenticate(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.rejectSessionAuthenticate(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun rejectSessionAuthenticate(
        params: Wallet.Params.RejectSessionAuthenticate,
        onSuccess: (Wallet.Params.RejectSessionAuthenticate) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.RejectAuthenticate(params.id, params.reason)
        SignClient.rejectAuthenticate(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.generateAuthObject(payloadParams, issuer, signature)")
    )
    @Throws(Exception::class)
    fun generateAuthObject(payloadParams: Wallet.Model.PayloadAuthRequestParams, issuer: String, signature: Wallet.Model.Cacao.Signature): Wallet.Model.Cacao {
        return com.walletconnect.sign.client.utils.generateAuthObject(payloadParams.toSign(), issuer, signature.toSign()).toWallet()
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.generateAuthPayloadParams(payloadParams, supportedChains, supportedMethods)")
    )
    @Throws(Exception::class)
    fun generateAuthPayloadParams(payloadParams: Wallet.Model.PayloadAuthRequestParams, supportedChains: List<String>, supportedMethods: List<String>): Wallet.Model.PayloadAuthRequestParams {
        return com.walletconnect.sign.client.utils.generateAuthPayloadParams(payloadParams.toSign(), supportedChains, supportedMethods).toWallet()
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.updateSession(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun updateSession(
        params: Wallet.Params.SessionUpdate,
        onSuccess: (Wallet.Params.SessionUpdate) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Update(params.sessionTopic, params.namespaces.toSign())
        SignClient.update(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.extendSession(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun extendSession(
        params: Wallet.Params.SessionExtend,
        onSuccess: (Wallet.Params.SessionExtend) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Extend(params.topic)
        SignClient.extend(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.respondSessionRequest(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun respondSessionRequest(
        params: Wallet.Params.SessionRequestResponse,
        onSuccess: (Wallet.Params.SessionRequestResponse) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Response(params.sessionTopic, params.jsonRpcResponse.toSign())
        SignClient.respond(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.emitSessionEvent(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun emitSessionEvent(
        params: Wallet.Params.SessionEmit,
        onSuccess: (Wallet.Params.SessionEmit) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Emit(params.topic, params.event.toSign(), params.chainId)
        SignClient.emit(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.disconnectSession(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun disconnectSession(
        params: Wallet.Params.SessionDisconnect,
        onSuccess: (Wallet.Params.SessionDisconnect) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        val signParams = Sign.Params.Disconnect(params.sessionTopic)
        SignClient.disconnect(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.pingSession(params, sessionPing)")
    )
    @Throws(IllegalStateException::class)
    fun pingSession(
        params: Wallet.Params.Ping,
        sessionPing: Wallet.Listeners.SessionPing?
    ) {
        val signParams = Sign.Params.Ping(params.sessionTopic)
        val signPingLister = object : Sign.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
                sessionPing?.onSuccess(Wallet.Model.Ping.Success(pingSuccess.topic))
            }

            override fun onError(pingError: Sign.Model.Ping.Error) {
                sessionPing?.onError(Wallet.Model.Ping.Error(pingError.error))
            }
        }

        SignClient.ping(signParams, signPingLister)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.formatAuthMessage(params)")
    )
    @Throws(IllegalStateException::class)
    fun formatAuthMessage(params: Wallet.Params.FormatAuthMessage): String {
        val signParams = Sign.Params.FormatMessage(params.payloadParams.toSign(), params.issuer)
        return SignClient.formatAuthMessage(signParams)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.formatAuthMessage(params)")
    )
    @Throws(IllegalStateException::class)
    fun formatMessage(params: Wallet.Params.FormatMessage): String? {
        val authParams = Auth.Params.FormatMessage(params.payloadParams.toAuth(), params.issuer)
        return AuthClient.formatMessage(authParams)
    }

    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.approveSessionAuthenticate(params, onSuccess, onError)")
    )
    @Throws(IllegalStateException::class)
    fun respondAuthRequest(
        params: Wallet.Params.AuthRequestResponse,
        onSuccess: (Wallet.Params.AuthRequestResponse) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit,
    ) {
        AuthClient.respond(params.toAuth(), { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getListOfActiveSessions()")
    )
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun getListOfActiveSessions(): List<Wallet.Model.Session> {
        return SignClient.getListOfActiveSessions().map(Sign.Model.Session::toWallet)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getActiveSessionByTopic()")
    )
    @Throws(IllegalStateException::class)
    fun getActiveSessionByTopic(topic: String): Wallet.Model.Session? {
        return SignClient.getActiveSessionByTopic(topic)?.toWallet()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getPendingSessionRequests(topic)")
    )
    @Throws(IllegalStateException::class)
    fun getPendingSessionRequests(topic: String): List<Wallet.Model.PendingSessionRequest> {
        return SignClient.getPendingRequests(topic).mapToPendingRequests()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getPendingListOfSessionRequests(topic)")
    )
    @Throws(IllegalStateException::class)
    fun getPendingListOfSessionRequests(topic: String): List<Wallet.Model.SessionRequest> {
        return SignClient.getPendingSessionRequests(topic).mapToPendingSessionRequests()
    }


    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getSessionProposals()")
    )
    @Throws(IllegalStateException::class)
    fun getSessionProposals(): List<Wallet.Model.SessionProposal> {
        return SignClient.getSessionProposals().map(Sign.Model.SessionProposal::toWallet)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("fun getPendingAuthenticateRequests(): List<Sign.Model.SessionAuthenticate> in Web3Wallet SDK")
    )
    @Throws(IllegalStateException::class)
    fun getPendingAuthRequests(): List<Wallet.Model.PendingAuthRequest> {
        return AuthClient.getPendingRequest().toWallet()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getVerifyContext(id)")
    )
    @Throws(IllegalStateException::class)
    fun getVerifyContext(id: Long): Wallet.Model.VerifyContext? {
        return SignClient.getVerifyContext(id)?.toWallet()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "Web3Wallet has been deprecated. Please use WalletKit instead from - https://github.com/reown-com/reown-kotlin",
        replaceWith = ReplaceWith("WalletKit.getListOfVerifyContexts()")
    )
    @Throws(IllegalStateException::class)
    fun getListOfVerifyContexts(): List<Wallet.Model.VerifyContext> {
        return SignClient.getListOfVerifyContexts().map { verifyContext -> verifyContext.toWallet() }
    }

    private fun validateInitializationCount(clientInitCounter: Int, onSuccess: () -> Unit, onError: (Wallet.Model.Error) -> Unit) {
        scope.launch {
            try {
                withTimeout(TIMEOUT) {
                    while (true) {
                        if (clientInitCounter == 2) {
                            onSuccess()
                            return@withTimeout
                        }
                        delay(100)
                    }
                }
            } catch (e: Exception) {
                onError(Wallet.Model.Error(e))
            }
        }
    }

    private const val TIMEOUT: Long = 10000
}