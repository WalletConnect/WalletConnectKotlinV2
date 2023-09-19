package com.walletconnect.sample.web3inbox.domain

import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.web3inbox.BuildConfig
import com.walletconnect.sample.web3inbox.domain.Web3InboxInitializer.wasNotifyDelegateInitalized
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleEvents
import com.walletconnect.sample.web3inbox.ui.routes.home.subscriptions.NotifyDelegate
import com.walletconnect.util.hexToBytes
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.web3.inbox.cacao.CacaoSigner
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.Web3Inbox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.web3j.utils.Numeric.toHexString
import timber.log.Timber

object Web3InboxInitializer {
    private lateinit var web3InboxInitializer: Web3InboxInitializerInstance

    val requestStatus: StateFlow<OnSignRequestStatus> by lazy { web3InboxInitializer.requestStatus }

    sealed interface OnSignRequestStatus {
        object NotSent : OnSignRequestStatus
        data class Success(val session: Modal.Model.Session) : OnSignRequestStatus
        data class Failure(val failure: Throwable) : OnSignRequestStatus
    }

    fun init(selectedAccount: String, random: EthAccount) {
//        if (::web3InboxInitializer.isInitialized) return

        web3InboxInitializer = Web3InboxInitializerInstance(selectedAccount, random)
    }

    var wasNotifyDelegateInitalized = false
}

class Web3InboxInitializerInstance(
    private val selectedAccount: String,
    private val random: EthAccount,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val sessionRequestMutex = Mutex()

    private fun String.getAddressFromCaip10() = this.split(':').last()
    private fun String.getNamespaceFromCaip10() = this.split(':').first()

    sealed interface OnSignResult {
        object Loading : OnSignResult
        data class Success(val result: String) : OnSignResult
        data class Failure(val failure: Throwable) : OnSignResult
    }

    private val _requestResult: MutableStateFlow<OnSignResult> = MutableStateFlow(OnSignResult.Loading)
    private val requestResult: StateFlow<OnSignResult> = _requestResult.asStateFlow()

    private val _requestStatus: MutableStateFlow<Web3InboxInitializer.OnSignRequestStatus> = MutableStateFlow(Web3InboxInitializer.OnSignRequestStatus.NotSent)
    val requestStatus: StateFlow<Web3InboxInitializer.OnSignRequestStatus> = _requestStatus.asStateFlow()

    private fun generatePersonalSignParams(message: String, selectedAccountInfo: String) = "[\"${toHexString(message.toByteArray())}\", \"$selectedAccountInfo\"]"

    private fun onSign(message: String): SignatureInterface {
        Timber.d("onSign(\"${message.take(200)}\")")

        if (message.contains(EthAccount.Fixed.address)) {
            return CacaoSigner.sign(message, EthAccount.Fixed.privateKey.hexToBytes(), SignatureType.EIP191)
        } else if (message.contains(random.address)) {
            return CacaoSigner.sign(message, random.privateKey.hexToBytes(), SignatureType.EIP191)
        } else if (message.contains(EthAccount.Burner.address)) {
            return CacaoSigner.sign(message, EthAccount.Burner.privateKey.hexToBytes(), SignatureType.EIP191)
        }

        return runBlocking(coroutineScope.coroutineContext) {
            Timber.d("params: ${generatePersonalSignParams(message, selectedAccount.getAddressFromCaip10())}")
            sessionRequestMutex.lock()
            val awaitRequestMutex = Mutex()

            val session: Modal.Model.Session = WalletConnectModal.getListOfActiveSessions()
                .first { session -> session.namespaces[selectedAccount.getNamespaceFromCaip10()]?.accounts?.firstOrNull { account -> account == selectedAccount } != null }

            WalletConnectModal.request(Modal.Params.Request(session.topic, "personal_sign", generatePersonalSignParams(message, selectedAccount.getAddressFromCaip10()), "eip155:1"),
                onSuccess = { coroutineScope.launch { _requestStatus.emit(Web3InboxInitializer.OnSignRequestStatus.Success(session)) } },
                onError = { error -> coroutineScope.launch { _requestStatus.emit(Web3InboxInitializer.OnSignRequestStatus.Failure(error.throwable)) }.also { Timber.e(error.throwable) } }
            )

            awaitRequestMutex.lock()
            val awaitRequestJob = requestResult.onEach { onSignResult ->
                when (onSignResult) {
                    OnSignResult.Loading -> {}
                    else -> awaitRequestMutex.unlock()

                }
            }.launchIn(coroutineScope)


            awaitRequestMutex.withLock {
                sessionRequestMutex.unlock()
                awaitRequestJob.cancel()
                val result = requestResult.value
                resetRequest()

                return@runBlocking when (result) {
                    is OnSignResult.Success -> {
                        Timber.d("Success: $result")
                        Inbox.Model.Cacao.Signature("eip191", result.result)
                    }

                    else -> Inbox.Model.Cacao.Signature("eip191", "")
                }
            }
        }
    }

    private suspend fun resetRequest() {
        _requestResult.emit(OnSignResult.Loading)
        _requestStatus.emit(Web3InboxInitializer.OnSignRequestStatus.NotSent)
    }

    init {
        WCMDelegate.wcEventModels.onEach { walletEvent: Modal.Model? ->
            when (walletEvent) {
                is Modal.Model.SessionRequestResponse -> {
                    when (walletEvent.result) {
                        is Modal.Model.JsonRpcResponse.JsonRpcResult -> {
                            Timber.d("Modal.Model.JsonRpcResponse.JsonRpcResult")
                            val successResult = (walletEvent.result as Modal.Model.JsonRpcResponse.JsonRpcResult)
                            _requestResult.emit(OnSignResult.Success(successResult.result))
                        }

                        is Modal.Model.JsonRpcResponse.JsonRpcError -> {
                            Timber.d("Modal.Model.JsonRpcResponse.JsonRpcError")
                            val errorResult = (walletEvent.result as Modal.Model.JsonRpcResponse.JsonRpcError)
                            _requestResult.emit(OnSignResult.Failure(Throwable(errorResult.message)))
                        }
                    }
                }

                else -> W3ISampleEvents.NoAction
            }
        }.shareIn(coroutineScope, SharingStarted.Eagerly)

        Web3Inbox.initialize(
            Inbox.Params.Init(
                core = CoreClient,
                account = Inbox.Type.AccountId(selectedAccount),
                onSign = { message -> onSign(message).let { signature -> Inbox.Model.Cacao.Signature(signature.t, signature.s, signature.m) } }
            ),
            onError = { error -> Timber.e(error.throwable) }
        )

        if (!wasNotifyDelegateInitalized) {
            wasNotifyDelegateInitalized = true
            NotifyClient.setDelegate(NotifyDelegate)

            val isLimited = false

            NotifyClient.register(
                Notify.Params.Registration(
                    selectedAccount,
                    isLimited = isLimited,
                    domain = BuildConfig.APPLICATION_ID,
                    onSign = { message -> onSign(message).let { signature -> Notify.Model.Cacao.Signature(signature.t, signature.s, signature.m) } }
                ),
                onSuccess = { identity ->
                    Timber.d("Registered isLimited: $isLimited identity: $identity")
                }, onError = {
                    Timber.e("Unable to register isLimited: $isLimited identity reason: " + it.throwable)
                }
            )
        }
    }
}