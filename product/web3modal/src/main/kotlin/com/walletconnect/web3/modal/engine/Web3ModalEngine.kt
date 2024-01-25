package com.walletconnect.web3.modal.engine


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseClient
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.Account
import com.walletconnect.web3.modal.client.models.CoinbaseClientAlreadyInitializedException
import com.walletconnect.web3.modal.client.models.request.Request
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import com.walletconnect.web3.modal.client.models.request.toSentRequest
import com.walletconnect.web3.modal.client.toCoinbaseSession
import com.walletconnect.web3.modal.client.toModal
import com.walletconnect.web3.modal.client.toSession
import com.walletconnect.web3.modal.client.toSign
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.domain.model.InvalidSessionException
import com.walletconnect.web3.modal.domain.model.Session
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionUseCase
import com.walletconnect.web3.modal.engine.coinbase.COINBASE_WALLET_ID
import com.walletconnect.web3.modal.utils.toAccount
import com.walletconnect.web3.modal.utils.toChain
import com.walletconnect.web3.modal.utils.toConnectorType
import kotlinx.coroutines.launch

internal class Web3ModalEngine(
    private val getSessionUseCase: GetSessionUseCase,
    private val getSelectedChainUseCase: GetSelectedChainUseCase,
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase,
) {
    internal var excludedWalletsIds: MutableList<String> = mutableListOf()
    internal var recommendedWalletsIds: MutableList<String> = mutableListOf()

    private lateinit var coinbaseClient: CoinbaseClient

    fun setup(
        init: Modal.Params.Init,
        onError: (Modal.Model.Error) -> Unit
    ) {
        excludedWalletsIds.addAll(init.excludedWalletIds)
        recommendedWalletsIds.addAll(init.recommendedWalletsIds)
        setupCoinbase(init, onError)
    }

    private fun setupCoinbase(init: Modal.Params.Init, onError: (Modal.Model.Error) -> Unit) {
        if (init.coinbaseEnabled) {
            if (!::coinbaseClient.isInitialized) {
                coinbaseClient = CoinbaseClient(context = wcKoinApp.koin.get(), appMetaData = wcKoinApp.koin.get())
            } else {
                onError(Modal.Model.Error(CoinbaseClientAlreadyInitializedException()))
            }
        } else {
            excludedWalletsIds.add(COINBASE_WALLET_ID)
        }
    }

    fun connectWC(
        connect: Modal.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        SignClient.connect(
            connect.toSign(),
            onSuccess
        ) { onError(it.throwable) }
    }

    fun connectCoinbase(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        checkEngineInitialization()
        coinbaseClient.connect(
            onSuccess = {
                Web3ModalDelegate.emit(it)
                onSuccess()
            }, onError = {
                onError(it)
            }
        )
    }

    fun getSelectedChain() = getSelectedChainUseCase()?.toChain()

    fun getActiveSession() = getSessionUseCase()?.isSessionActive()

    private fun Session.isSessionActive() = when (this) {
        is Session.Coinbase -> if (coinbaseClient.isConnected()) this else null
        is Session.WalletConnect -> SignClient.getActiveSessionByTopic(topic)?.let { this }
    }

    fun getConnectorType() = getSessionUseCase()?.toConnectorType()

    internal fun getSelectedChainOrFirst() = getSelectedChain() ?: Web3Modal.chains.first()

    fun request(request: Request, onSuccess: (SentRequestResult) -> Unit, onError: (Throwable) -> Unit) {
        val session = getActiveSession()
        val selectedChain = getSelectedChain()

        if (session == null || selectedChain == null) {
            onError(InvalidSessionException)
            return
        }

        when (session) {
            is Session.Coinbase -> {
                checkEngineInitialization()
                coinbaseClient.request(request, { onSuccess(SentRequestResult.Coinbase(request.method, request.params, selectedChain.id, it)) }, onError)
            }

            is Session.WalletConnect -> SignClient.request(
                request.toSign(session.topic, selectedChain.id),
                {
                    onSuccess(it.toSentRequest())
                    openWalletApp(session.topic, onError)
                },
                { onError(it.throwable) }
            )
        }
    }

    private fun openWalletApp(topic: String, onError: (Throwable) -> Unit) {
        val redirect = SignClient.getActiveSessionByTopic(topic)?.redirect ?: String.Empty
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirect))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            wcKoinApp.koin.get<Context>().startActivity(intent)
        } catch (e: Throwable) {
            onError(e)
        }

    }

    fun ping(sessionPing: Modal.Listeners.SessionPing?) {
        when (val session = getSessionUseCase()) {
            is Session.WalletConnect -> SignClient.ping(Sign.Params.Ping(session.topic), sessionPing?.toSign())
            else -> sessionPing?.onError(Modal.Model.Ping.Error(InvalidSessionException))
        }
    }

    fun disconnect(onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit) {
        val session = getSessionUseCase()

        if (session == null) {
            onError(InvalidSessionException)
            return
        }
        scope.launch { deleteSessionDataUseCase() }
        when (session) {
            is Session.Coinbase -> {
                checkEngineInitialization()
                coinbaseClient.disconnect()
                onSuccess()
            }

            is Session.WalletConnect -> SignClient.disconnect(Sign.Params.Disconnect(session.topic), { onSuccess() }, { onError(it.throwable) })
        }
    }

    fun clearSession() {
        scope.launch { deleteSessionDataUseCase() }
    }

    fun getAccount(): Account? = getActiveSession()?.let { session ->
        when (session) {
            is Session.Coinbase -> coinbaseClient.getAccount(session)
            is Session.WalletConnect -> SignClient.getActiveSessionByTopic(session.topic)?.toAccount(session)
        }
    }

    fun getSession() = getSessionUseCase()?.let { session ->
        when(session) {
            is Session.Coinbase -> coinbaseClient.getAccount(session)?.toCoinbaseSession()
            is Session.WalletConnect -> SignClient.getActiveSessionByTopic(session.topic)?.toSession()
        }
    }

    @Throws(IllegalStateException::class)
    fun setInternalDelegate(delegate: Web3ModalDelegate) {
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
                clearSession()
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

    fun registerCoinbaseLauncher(activity: ComponentActivity) {
        if (::coinbaseClient.isInitialized) {
            coinbaseClient.setLauncher(activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> result.data?.data?.let { coinbaseClient.handleResponse(it) } })
        }
    }

    fun unregisterCoinbase() {
        if (::coinbaseClient.isInitialized) {
            coinbaseClient.unregister()
        }
    }

    fun coinbaseIsEnabled() = ::coinbaseClient.isInitialized && coinbaseClient.isInstalled() && coinbaseClient.isLauncherSet()

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::coinbaseClient.isInitialized) {
            "Coinbase Client needs to be initialized first using the initialize function"
        }
    }
}
