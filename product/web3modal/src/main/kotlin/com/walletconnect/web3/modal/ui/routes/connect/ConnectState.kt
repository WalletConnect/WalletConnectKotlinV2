package com.walletconnect.web3.modal.ui.routes.connect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.modal.domain.usecase.GetAllWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.web3.modal.ui.model.UiState
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.connection.navigateToRedirect
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val W3M_SDK = "w3m"

@Composable
internal fun rememberConnectState(
    coroutineScope: CoroutineScope,
    navController: NavController
): ConnectState {
    return remember(coroutineScope, navController) {
        ConnectState(coroutineScope, navController)
    }
}

internal class ConnectState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController
) {
    private val logger: Logger = wcKoinApp.koin.get()
    private val getWalletsUseCase: GetAllWalletsUseCaseInterface = wcKoinApp.koin.get()
    private val getRecentWalletUseCase: GetRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveRecentWalletUseCase: SaveRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()

    private val pairing by lazy {
        CoreClient.Pairing.create { error ->
            throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
        }!!
    }

    private var sessionParams = getSessionParamsSelectedChain()

    var wallets: List<Wallet> = listOf()

    val uri: String
        get() = pairing.uri

    val selectedChain = observeSelectedChainUseCase().map { savedChainId ->
        Web3Modal.chains.find { it.id == savedChainId } ?: Web3Modal.getSelectedChainOrFirst()
    }


    fun navigateToScanQRCode() = connect {
        coroutineScope.launch(Dispatchers.Main) { navController.navigate(Route.QR_CODE.path) }
    }

    fun navigateToRedirectRoute(wallet: Wallet) {
        saveRecentWalletUseCase(wallet.id)
        navController.navigateToRedirect(wallet)
    }

    fun navigateToConnectWallet(chain: Modal.Model.Chain) {
        coroutineScope.launch { saveChainSelectionUseCase(chain.id) }
        Web3Modal.selectedChain = chain
        sessionParams = getSessionParamsSelectedChain()
        navController.navigate(Route.CONNECT_YOUR_WALLET.path)
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
                onError = { logger.error(it.throwable) }
            )
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    internal fun getWallets() = flow<UiState<List<Wallet>>> {
        if (wallets.isEmpty()) {
            wallets = fetchWallets()
        }
        emit(UiState.Success(wallets))
    }
        .catch {
            logger.error(it)
            emit(UiState.Error(it))
        }
        .flowOn(Dispatchers.IO)

    private suspend fun fetchWallets() = getWalletsUseCase(sdkType = W3M_SDK, Web3Modal.excludedWalletsIds, Web3Modal.recommendedWalletsIds).mapRecentWallet(getRecentWalletUseCase())

    private fun getSessionParamsSelectedChain() = with(Web3Modal.chains) {
        val selectedChain = runBlocking { Web3Modal.chains.getSelectedChain(getSelectedChainUseCase()) }
        Modal.Params.SessionParams(
            requiredNamespaces = mapOf(
                selectedChain.chainNamespace to Modal.Model.Namespace.Proposal(
                    chains = listOf(selectedChain.id),
                    methods = selectedChain.methods,
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
                methods = value.flatMap { it.methods }.distinct(),
                events = value.flatMap { it.events }.distinct()
            )
        }.toMap()
}

private fun List<Wallet>.mapRecentWallet(id: String?) = map {
    it.apply { it.isRecent = it.id == id }
}.sortedWith(compareByDescending<Wallet> { it.isRecent }.thenByDescending { it.isWalletInstalled })
