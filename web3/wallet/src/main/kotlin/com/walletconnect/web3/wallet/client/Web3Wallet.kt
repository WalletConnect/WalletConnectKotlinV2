package com.walletconnect.web3.wallet.client

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.utils.addSdkBitsetForUA
import kotlinx.coroutines.*
import org.koin.dsl.module
import java.util.*

object Web3Wallet {
    private lateinit var coreClient: CoreClient

    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, sessionContext: Wallet.Model.SessionContext)
        fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, sessionContext: Wallet.Model.SessionContext)
        fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete)
        fun onAuthRequest(authRequest: Wallet.Model.AuthRequest, authContext: Wallet.Model.AuthContext)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse)

        //Utils
        fun onConnectionStateChange(state: Wallet.Model.ConnectionState)
        fun onError(error: Wallet.Model.Error)
    }

    @Throws(IllegalStateException::class)
    fun setWalletDelegate(delegate: WalletDelegate) {

        val signWalletDelegate = object : SignClient.WalletDelegate {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, sessionContext: Sign.Model.SessionContext) {
                delegate.onSessionProposal(sessionProposal.toWallet(), sessionContext.toWallet())
            }

            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, sessionContext: Sign.Model.SessionContext) {
                delegate.onSessionRequest(sessionRequest.toWallet(), sessionContext.toWallet())
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                delegate.onSessionDelete(deletedSession.toWallet())
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                delegate.onSessionSettleResponse(settleSessionResponse.toWallet())
            }

            override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
                delegate.onSessionUpdateResponse(sessionUpdateResponse.toWallet())
            }

            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
                delegate.onConnectionStateChange(Wallet.Model.ConnectionState(state.isAvailable))
            }

            override fun onError(error: Sign.Model.Error) {
                delegate.onError(Wallet.Model.Error(error.throwable))
            }
        }

        val authWalletDelegate = object : AuthClient.ResponderDelegate {
            override fun onAuthRequest(authRequest: Auth.Event.AuthRequest, authContext: Auth.Event.AuthContext) {
                delegate.onAuthRequest(authRequest.toWallet(), authContext.toWallet())
            }

            override fun onConnectionStateChange(connectionStateChange: Auth.Event.ConnectionStateChange) {
                //ignore
            }

            override fun onError(error: Auth.Event.Error) {
                delegate.onError(Wallet.Model.Error(error.error.throwable))
            }

        }

        SignClient.setWalletDelegate(signWalletDelegate)
        AuthClient.setResponderDelegate(authWalletDelegate)
    }

    @Throws(IllegalStateException::class)
    fun initialize(params: Wallet.Params.Init, onSuccess: () -> Unit = {}, onError: (Wallet.Model.Error) -> Unit) {
        coreClient = params.core
        wcKoinApp.modules(
            module { addSdkBitsetForUA(bitset) }
        )
        var clientInitCounter = 0
        SignClient.initialize(Sign.Params.Init(params.core), onSuccess = { clientInitCounter++ }) { error -> onError(Wallet.Model.Error(error.throwable)) }
        AuthClient.initialize(Auth.Params.Init(params.core), onSuccess = { clientInitCounter++ }) { error -> onError(Wallet.Model.Error(error.throwable)) }
        validateInitializationCount(clientInitCounter, onSuccess, onError)
    }

    @Throws(IllegalStateException::class)
    fun registerDeviceToken(firebaseAccessToken: String, onSuccess: () -> Unit, onError: (Wallet.Model.Error) -> Unit) {
        coreClient.Echo.register(firebaseAccessToken, onSuccess) { error -> onError(Wallet.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    fun pair(params: Wallet.Params.Pair, onSuccess: (Wallet.Params.Pair) -> Unit = {}, onError: (Wallet.Model.Error) -> Unit = {}) {
        coreClient.Pairing.pair(Core.Params.Pair(params.uri), { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(IllegalStateException::class)
    fun approveSession(
        params: Wallet.Params.SessionApprove,
        onSuccess: (Wallet.Params.SessionApprove) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Approve(params.proposerPublicKey, params.namespaces.toSign(), params.relayProtocol)
        SignClient.approveSession(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(Exception::class)
    fun generateApprovedNamespaces(sessionProposal: Wallet.Model.SessionProposal, supportedNamespaces: Map<String, Wallet.Model.Namespace.Session>): Map<String, Wallet.Model.Namespace.Session> {
        return com.walletconnect.sign.client.utils.generateApprovedNamespaces(sessionProposal.toSign(), supportedNamespaces.toSign()).toWallet()
    }

    @Throws(IllegalStateException::class)
    fun rejectSession(
        params: Wallet.Params.SessionReject,
        onSuccess: (Wallet.Params.SessionReject) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Reject(params.proposerPublicKey, params.reason)
        SignClient.rejectSession(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(IllegalStateException::class)
    fun updateSession(
        params: Wallet.Params.SessionUpdate,
        onSuccess: (Wallet.Params.SessionUpdate) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Update(params.sessionTopic, params.namespaces.toSign())
        SignClient.update(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(IllegalStateException::class)
    fun extendSession(
        params: Wallet.Params.SessionExtend,
        onSuccess: (Wallet.Params.SessionExtend) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Extend(params.topic)
        SignClient.extend(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(IllegalStateException::class)
    fun respondSessionRequest(
        params: Wallet.Params.SessionRequestResponse,
        onSuccess: (Wallet.Params.SessionRequestResponse) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Response(params.sessionTopic, params.jsonRpcResponse.toSign())
        SignClient.respond(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(IllegalStateException::class)
    fun emitSessionEvent(
        params: Wallet.Params.SessionEmit,
        onSuccess: (Wallet.Params.SessionEmit) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Emit(params.topic, params.event.toSign(), params.chainId)
        SignClient.emit(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    @Throws(IllegalStateException::class)
    fun disconnectSession(
        params: Wallet.Params.SessionDisconnect,
        onSuccess: (Wallet.Params.SessionDisconnect) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        val signParams = Sign.Params.Disconnect(params.sessionTopic)
        SignClient.disconnect(signParams, { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Throws(IllegalStateException::class)
    fun formatMessage(params: Wallet.Params.FormatMessage): String? {
        val authParams = Auth.Params.FormatMessage(params.payloadParams.toSign(), params.issuer)
        return AuthClient.formatMessage(authParams)
    }

    @Throws(IllegalStateException::class)
    fun respondAuthRequest(
        params: Wallet.Params.AuthRequestResponse,
        onSuccess: (Wallet.Params.AuthRequestResponse) -> Unit = {},
        onError: (Wallet.Model.Error) -> Unit
    ) {
        AuthClient.respond(params.toAuth(), { onSuccess(params) }, { error -> onError(Wallet.Model.Error(error.throwable)) })
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Throws(IllegalStateException::class)
    fun getListOfActiveSessions(): List<Wallet.Model.Session> {
        return SignClient.getListOfActiveSessions().map(Sign.Model.Session::toWallet)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Throws(IllegalStateException::class)
    fun getActiveSessionByTopic(topic: String): Wallet.Model.Session? {
        return SignClient.getActiveSessionByTopic(topic)?.toWallet()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */

    @Deprecated(
        "The return type of getPendingRequests methods has been replaced with SessionRequest list",
        replaceWith = ReplaceWith("getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest>")
    )
    @Throws(IllegalStateException::class)
    fun getPendingSessionRequests(topic: String): List<Wallet.Model.PendingSessionRequest> {
        return SignClient.getPendingRequests(topic).mapToPendingRequests()
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */

    @Throws(IllegalStateException::class)
    fun getPendingListOfSessionRequests(topic: String): List<Wallet.Model.SessionRequest> {
        return SignClient.getPendingSessionRequests(topic).mapToPendingSessionRequests()
    }


    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Throws(IllegalStateException::class)
    fun getSessionProposals(): List<Wallet.Model.SessionProposal> {
        return SignClient.getSessionProposals().map(Sign.Model.SessionProposal::toWallet)
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Throws(IllegalStateException::class)
    fun getPendingAuthRequests(): List<Wallet.Model.PendingAuthRequest> {
        return AuthClient.getPendingRequest().toWallet()
    }

    private fun validateInitializationCount(clientInitCounter: Int, onSuccess: () -> Unit, onError: (Wallet.Model.Error) -> Unit) {
        scope.launch {
            try {
                withTimeout(TIMEOUT) {
                    while (true) {
                        if (clientInitCounter == 2) {
                            onSuccess()
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                onError(Wallet.Model.Error(e))
            }
        }
    }

    private const val TIMEOUT: Long = 10000
    private const val BIT_ORDER = 4 // https://github.com/WalletConnect/walletconnect-docs/blob/main/docs/specs/clients/core/relay/relay-user-agent.md#schema
    private val bitset: BitSet
        get() = BitSet().apply {
            set(BIT_ORDER)
        }
}
