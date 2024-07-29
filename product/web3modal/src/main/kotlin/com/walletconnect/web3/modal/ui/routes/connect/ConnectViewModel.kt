package com.walletconnect.web3.modal.ui.routes.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pulse.domain.SendEventInterface
import com.walletconnect.android.pulse.model.ConnectionMethod
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import com.walletconnect.modal.ui.model.LoadingState
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.Request
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.web3.modal.engine.Web3ModalEngine
import com.walletconnect.web3.modal.ui.navigation.Navigator
import com.walletconnect.web3.modal.ui.navigation.NavigatorImpl
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.connection.toRedirectPath
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ConnectViewModel : ViewModel(), Navigator by NavigatorImpl(), ParingController by PairingControllerImpl() {
    private val logger: Logger = wcKoinApp.koin.get()
    private val walletsDataStore = WalletDataSource { showError(it) }
    private val saveRecentWalletUseCase: SaveRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()
    private val web3ModalEngine: Web3ModalEngine = wcKoinApp.koin.get()
    private val sendEventUseCase: SendEventInterface = wcKoinApp.koin.get()
    private var sessionParams = getSessionParamsSelectedChain(Web3Modal.selectedChain?.id)
    val selectedChain = observeSelectedChainUseCase().map { savedChainId ->
        Web3Modal.chains.find { it.id == savedChainId } ?: web3ModalEngine.getSelectedChainOrFirst()
    }
    private var _isConfirmLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isConfirmLoading get() = _isConfirmLoading.asStateFlow()
    private var _isCancelLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isCancelLoading get() = _isCancelLoading.asStateFlow()
    var wallet: Wallet? = null

    val walletsState: StateFlow<WalletsData> = walletsDataStore.searchWalletsState.stateIn(viewModelScope, SharingStarted.Lazily, WalletsData.empty())
    val uiState: StateFlow<UiState<List<Wallet>>> = walletsDataStore.walletState.map { pagingData ->
        when {
            pagingData.error != null -> UiState.Error(pagingData.error)
            pagingData.loadingState == LoadingState.REFRESH -> UiState.Loading()
            else -> UiState.Success(pagingData.wallets)
        }
    }.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

    val searchPhrase
        get() = walletsDataStore.searchPhrase

    init {
        Web3ModalDelegate
            .wcEventModels
            .filterIsInstance<Modal.Model.SIWEAuthenticateResponse.Error>()
            .onEach {
                _isConfirmLoading.value = false
                showError(it.message)
                disconnect()
            }.launchIn(viewModelScope)

        fetchInitialWallets()
    }

    fun disconnect() {
        _isCancelLoading.value = true
        web3ModalEngine.disconnect(
            onSuccess = {
                _isCancelLoading.value = false
                closeModal()
            },
            onError = {
                _isCancelLoading.value = false
                showError(it.localizedMessage)
                logger.error(it)
            }
        )
    }

    fun sendSIWEOverPersonalSign() {
        _isConfirmLoading.value = true
        web3ModalEngine.shouldDisconnect = false
        val account = web3ModalEngine.getAccount() ?: throw IllegalStateException("Account is null")
        val issuer = "did:pkh:${account.chain.id}:${account.address}"
        val siweMessage = web3ModalEngine.formatSIWEMessage(Web3Modal.authPayloadParams!!, issuer)
        val msg = siweMessage.encodeToByteArray().joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
        val body = "[\"$msg\", \"${account.address}\"]"
        web3ModalEngine.request(
            request = Request("personal_sign", body),
            onSuccess = { sendRequest ->
                logger.log("SIWE sent successfully")
                web3ModalEngine.siweRequestIdWithMessage = Pair((sendRequest as SentRequestResult.WalletConnect).requestId, siweMessage)
            },
            onError = {
                if (it !is Web3ModalEngine.RedirectMissingThrowable) {
                    web3ModalEngine.shouldDisconnect = true
                }

                _isConfirmLoading.value = false
                showError(it.message)
            },
        )
    }

    fun fetchInitialWallets() {
        viewModelScope.launch { walletsDataStore.fetchInitialWallets() }
    }

    fun navigateToHelp() {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CLICK_NETWORK_HELP))
        navigateTo(Route.WHAT_IS_WALLET.path)
    }

    fun navigateToScanQRCode() {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.SELECT_WALLET, Properties(name = "WalletConnect", platform = ConnectionMethod.QR_CODE)))
        connectWalletConnect(name = "WalletConnect", method = ConnectionMethod.QR_CODE, linkMode = null) { navigateTo(Route.QR_CODE.path) }
    }

    fun navigateToRedirectRoute(wallet: Wallet) {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.SELECT_WALLET, Properties(name = wallet.name, platform = wallet.toConnectionType())))
        saveRecentWalletUseCase(wallet.id)
        walletsDataStore.updateRecentWallet(wallet.id)
        navigateTo(wallet.toRedirectPath())
    }

    fun navigateToConnectWallet(chain: Modal.Model.Chain) {
        viewModelScope.launch { saveChainSelectionUseCase(chain.id) }
        sessionParams = getSessionParamsSelectedChain(chain.id)
        navigateTo(Route.CONNECT_YOUR_WALLET.path)
    }

    fun navigateToAllWallets() {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CLICK_ALL_WALLETS))
        clearSearch()
        navigateTo(Route.ALL_WALLETS.path)
    }

    fun connectWalletConnect(name: String, method: String, linkMode: String?, onSuccess: (String) -> Unit) {
        if (Web3Modal.authPayloadParams != null) {
            authenticate(
                name, method,
                walletAppLink = linkMode,
                authParams = if (Web3Modal.selectedChain != null) Web3Modal.authPayloadParams!!.copy(chains = listOf(Web3Modal.selectedChain!!.id)) else Web3Modal.authPayloadParams!!,
                onSuccess = { onSuccess(it) },
                onError = {
                    sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CONNECT_ERROR, Properties(message = it.message ?: "Relay error while connecting")))
                    showError(it.localizedMessage)
                    logger.error(it)
                }
            )
        } else {
            connect(
                name, method,
                sessionParams = sessionParams,
                onSuccess = onSuccess,
                onError = {
                    sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CONNECT_ERROR, Properties(message = it.message ?: "Relay error while connecting")))
                    showError(it.localizedMessage)
                    logger.error(it)
                }
            )
        }
    }

    fun connectCoinbase(onSuccess: () -> Unit = {}) {
        web3ModalEngine.connectCoinbase(
            onSuccess = onSuccess,
            onError = {
                showError(it.localizedMessage)
                logger.error(it)
            }
        )
    }

    fun fetchMoreWallets() {
        viewModelScope.launch { walletsDataStore.fetchMoreWallets() }
    }

    fun search(searchPhrase: String) {
        viewModelScope.launch { walletsDataStore.searchWallet(searchPhrase) }
    }

    fun clearSearch() = walletsDataStore.clearSearch()

    fun getWallet(walletId: String?) = walletsDataStore.getWallet(walletId).also { wallet = it }

    fun getNotInstalledWallets() = walletsDataStore.wallets.filterNot { it.isWalletInstalled }

    fun getWalletsTotalCount() = walletsDataStore.totalWalletsCount

    private fun Wallet.toConnectionType(): String {
        if (isWalletInstalled) ConnectionMethod.MOBILE

        return when {
            hasMobileWallet && hasWebApp -> ConnectionMethod.UNDEFINED
            hasMobileWallet -> ConnectionMethod.MOBILE
            hasWebApp -> ConnectionMethod.WEB
            else -> ConnectionMethod.UNDEFINED
        }
    }

    private fun getSessionParamsSelectedChain(chainId: String?) = with(Web3Modal.chains) {
        val selectedChain = getSelectedChain(chainId)
        Modal.Params.SessionParams(
            requiredNamespaces = mapOf(
                selectedChain.chainNamespace to Modal.Model.Namespace.Proposal(
                    chains = listOf(selectedChain.id),
                    methods = selectedChain.requiredMethods,
                    events = selectedChain.events
                )
            ),
            optionalNamespaces = filter { it.id != selectedChain.id }.toOptionalNamespaces()
        )
    }

    private fun List<Modal.Model.Chain>.toOptionalNamespaces() = groupBy { it.chainNamespace }
        .map { (key: String, value: List<Modal.Model.Chain>) ->
            key to Modal.Model.Namespace.Proposal(
                chains = value.map { it.id },
                methods = value.flatMap { it.requiredMethods + it.optionalMethods }.distinct(),
                events = value.flatMap { it.events }.distinct()
            )
        }.toMap()
}
