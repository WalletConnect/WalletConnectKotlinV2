package com.walletconnect.sample.web3inbox.ui.routes.home.web3inbox

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.sample.web3inbox.domain.W3MDelegate
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleEvents
import com.walletconnect.sample.web3inbox.ui.routes.accountArg
import com.walletconnect.util.hexToBytes
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.web3.inbox.cacao.CacaoSigner
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.Web3Inbox
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

class Web3InboxViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val selectedAccount = checkNotNull(savedStateHandle.get<String>(accountArg))
    lateinit var random: EthAccount
    private val sessionRequestMutex = Mutex()

    private fun String.getAddressFromCaip10() = this.split(':').last()

    sealed interface OnSignResult {
        object Loading : OnSignResult
        data class Success(val result: String) : OnSignResult
        data class Failure(val failure: Throwable) : OnSignResult
    }

    sealed interface OnSignRequestStatus {
        object NotSent : OnSignRequestStatus
        data class Success(val session: Modal.Model.Session) : OnSignRequestStatus
        data class Failure(val failure: Throwable) : OnSignRequestStatus
    }

    private val _requestResult: MutableStateFlow<OnSignResult> = MutableStateFlow(OnSignResult.Loading)
    private val requestResult: StateFlow<OnSignResult> = _requestResult.asStateFlow()

    private val _requestStatus: MutableStateFlow<OnSignRequestStatus> = MutableStateFlow(OnSignRequestStatus.NotSent)
    val requestStatus: StateFlow<OnSignRequestStatus> = _requestStatus.asStateFlow()

    private fun generatePersonalSignParams(message: String, selectedAccountInfo: String) = "[\"${toHexString(message.toByteArray())}\", \"$selectedAccountInfo\"]"

    private fun onSign(message: String): Inbox.Model.Cacao.Signature {
        Timber.d("onSign")

        if (message.contains(EthAccount.Fixed.address)) {
            return CacaoSigner.sign(message, EthAccount.Fixed.privateKey.hexToBytes(), SignatureType.EIP191)
        } else if (message.contains(random.address)) {
            return CacaoSigner.sign(message, random.privateKey.hexToBytes(), SignatureType.EIP191)
        } else if (message.contains(EthAccount.Burner.address)) {
            return CacaoSigner.sign(message, EthAccount.Burner.privateKey.hexToBytes(), SignatureType.EIP191)
        }

        return runBlocking(viewModelScope.coroutineContext) {
            Timber.d("runBlocking started")
            Timber.d("params: ${generatePersonalSignParams(message, selectedAccount.getAddressFromCaip10())}")
            sessionRequestMutex.lock()
            val localMutex = Mutex()
            Timber.d("sessionRequestMutex: $sessionRequestMutex locked")

            val session: Modal.Model.Session = WalletConnectModal.getListOfActiveSessions().first()
            WalletConnectModal.request(Modal.Params.Request(session.topic, "personal_sign", generatePersonalSignParams(message, selectedAccount.getAddressFromCaip10()), "eip155:1"),
                onSuccess = { viewModelScope.launch { _requestStatus.emit(OnSignRequestStatus.Success(session)) } },
                onError = { error -> viewModelScope.launch { _requestStatus.emit(OnSignRequestStatus.Failure(error.throwable)) }.also { Timber.e(error.throwable) } }
            )

            localMutex.lock()
            Timber.d("localMutex: $localMutex locked")
            val awaitRequestJob = requestResult.onEach { onSignResult ->
                Timber.d("requestResult.onEach")
                when (onSignResult) {
                    OnSignResult.Loading -> {}
                    else -> localMutex.unlock().also {
                        Timber.d("localMutex: $localMutex unlocked")
                    }
                }
            }.launchIn(viewModelScope)


            Timber.d("after async")
            localMutex.withLock {
                Timber.d("mutex.withLock")

                sessionRequestMutex.unlock().also {
                    Timber.d("sessionRequestMutex: $sessionRequestMutex unlocked")
                }
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
        _requestStatus.emit(OnSignRequestStatus.NotSent)
    }

    init {
        W3MDelegate.wcEventModels.onEach { walletEvent: Modal.Model? ->
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
        }.shareIn(viewModelScope, SharingStarted.Eagerly)

        viewModelScope.launch {

            Web3Inbox.initialize(
                Inbox.Params.Init(
                    core = CoreClient,
                    account = Inbox.Type.AccountId(selectedAccount),
                    onSign = ::onSign
                ), onError = { error -> Timber.e(error.throwable) }
            )
        }
    }
}