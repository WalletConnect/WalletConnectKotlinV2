package com.walletconnect.web3.modal.ui.routes.connect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.web3.modal.ui.model.LoadingState
import com.walletconnect.web3.modal.ui.model.UiState
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.connection.navigateToRedirect
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
internal fun rememberConnectState(
    coroutineScope: CoroutineScope,
    navController: NavController,
    showError: (String?) -> Unit
): ConnectState {
    return remember(coroutineScope, navController) {
        ConnectState(coroutineScope, navController, showError)
    }
}

internal class ConnectState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController,
    private val showError: (String?) -> Unit
) {
    private val logger: Logger = wcKoinApp.koin.get()
    private val walletsDataStore = WalletDataSource(showError)
    private val saveRecentWalletUseCase: SaveRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()

    private val pairing by lazy {
        CoreClient.Pairing.create { error ->
            throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
        }!!
    }

    private var sessionParams = getSessionParamsSelectedChain(getSelectedChainUseCase())

    val uri: String
        get() = pairing.uri

    val selectedChain = observeSelectedChainUseCase().map { savedChainId ->
        Web3Modal.chains.find { it.id == savedChainId } ?: Web3Modal.getSelectedChainOrFirst()
    }

    val walletsState: StateFlow<WalletsData> = walletsDataStore.searchWalletsState.stateIn(coroutineScope, SharingStarted.Lazily, WalletsData.empty())

    val uiState: StateFlow<UiState<List<Wallet>>> = walletsDataStore.walletState.map { pagingData ->
        when {
            pagingData.error != null -> UiState.Error(pagingData.error)
            pagingData.loadingState == LoadingState.REFRESH -> UiState.Loading()
            else -> UiState.Success(pagingData.wallets)
        }
    }.stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

    val searchPhrase
        get() = walletsDataStore.searchPhrase

    init {
        fetchInitialWallets()
    }

    fun fetchInitialWallets() {
        coroutineScope.launch { walletsDataStore.fetchInitialWallets() }
    }

    fun navigateToHelp() {
        navController.navigate(Route.WHAT_IS_WALLET.path)
    }

    fun navigateToScanQRCode() = connect {
        coroutineScope.launch(Dispatchers.Main) { navController.navigate(Route.QR_CODE.path) }
    }

    fun navigateToRedirectRoute(wallet: Wallet) {
        saveRecentWalletUseCase(wallet.id)
        walletsDataStore.updateRecentWallet(wallet.id)
        navController.navigateToRedirect(wallet)
    }

    fun navigateToConnectWallet(chain: Modal.Model.Chain) {
        coroutineScope.launch { saveChainSelectionUseCase(chain.id) }
        sessionParams = getSessionParamsSelectedChain(chain.id)
        navController.navigate(Route.CONNECT_YOUR_WALLET.path)
    }

    fun navigateToAllWallets() {
        clearSearch()
        navController.navigate(Route.ALL_WALLETS.path)
    }

    fun connect(onSuccess: (String) -> Unit) {
        try {
            val connectParams = Modal.Params.Connect(
                sessionParams.requiredNamespaces,
                sessionParams.optionalNamespaces,
                sessionParams.properties,
                pairing
            )
            Web3Modal.connect(
                connect = connectParams,
                onSuccess = { onSuccess(pairing.uri) },
                onError = {
                    showError(it.throwable.localizedMessage)
                    logger.error(it.throwable)
                }
            )
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    fun fetchMoreWallets() {
        coroutineScope.launch { walletsDataStore.fetchMoreWallets() }
    }

    fun search(searchPhrase: String) {
        coroutineScope.launch { walletsDataStore.searchWallet(searchPhrase) }
    }

    fun clearSearch() = walletsDataStore.clearSearch()

    fun getWallet(walletId: String?) = walletsDataStore.getWallet(walletId)

    fun getNotInstalledWallets() = walletsState.value.wallets.filterNot { it.isWalletInstalled }

    fun getWalletsTotalCount() = walletsDataStore.totalWalletsCount

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
