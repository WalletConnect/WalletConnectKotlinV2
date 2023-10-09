package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Balance
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetEthBalanceUseCase
import com.walletconnect.web3.modal.domain.usecase.GetIdentityUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.ui.model.UiState
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.account.navigateToChainSwitch
import com.walletconnect.web3.modal.utils.getAddress
import com.walletconnect.web3.modal.utils.getChains
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
internal fun rememberAccountState(
    coroutineScope: CoroutineScope,
    navController: NavController,
    closeModal: () -> Unit
): AccountState = remember(coroutineScope, navController) {
    AccountState(coroutineScope, navController, closeModal)
}

internal class AccountState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController,
    val closeModal: () -> Unit
) {
    private val logger: Logger = wcKoinApp.koin.get()

    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()
    private val observeSessionTopicUseCase: ObserveSessionTopicUseCase = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()
    private val getIdentityUseCase: GetIdentityUseCase = wcKoinApp.koin.get()
    private val getEthBalanceUseCase: GetEthBalanceUseCase = wcKoinApp.koin.get()

    private val activeSessionFlow = observeSessionTopicUseCase()
        .map { topic -> topic?.let { Web3Modal.getActiveSessionByTopic(topic) } }

    private val accountDataFlow = activeSessionFlow
        .map { activeSession ->
            if (activeSession != null) {
                val chains = activeSession.getChains()
                val selectedChain = chains.getSelectedChain(getSelectedChainUseCase())
                val address = activeSession.getAddress(selectedChain)
                val identity = getIdentityUseCase(address, selectedChain.id)
                accountData = AccountData(
                    topic = activeSession.topic,
                    address = address,
                    chains = chains,
                    identity = identity
                )
                UiState.Success(accountData)
            } else {
                UiState.Error(Throwable("Active session not found"))
            }
        }.catch {
            logger.error(it)
            emit(UiState.Error(it))
        }

    lateinit var accountData: AccountData

    val accountState = accountDataFlow.stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

    val selectedChain = observeSelectedChainUseCase().map { savedChainId ->
        Web3Modal.chains.find { it.id == savedChainId } ?: Web3Modal.getSelectedChainOrFirst()
    }

    val balanceState = combine(activeSessionFlow, selectedChain) { session, selectedChain ->
        if (session != null && selectedChain.rpcUrl != null) {
            return@combine getEthBalanceUseCase(selectedChain.token, selectedChain.rpcUrl, session.getAddress(selectedChain))
        } else {
            null
        }
    }
        .flowOn(Dispatchers.IO)
        .catch { logger.error(it) }
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = null)

    fun disconnect(
        topic: String,
        onSuccess: () -> Unit
    ) {
        Web3Modal.disconnect(
            disconnect = Modal.Params.Disconnect(topic),
            onSuccess = {
                coroutineScope.launch { deleteSessionDataUseCase() }
                coroutineScope.launch(Dispatchers.Main) { onSuccess() }
            },
            onError = {
                logger.error(it.throwable)
            }
        )
    }

    fun changeActiveChain(chain: Modal.Model.Chain) = coroutineScope.launch {
        saveChainSelectionUseCase(chain.id).also { Web3Modal.selectedChain = chain }
        navController.popBackStack()
        if (navController.currentDestination == null) {
            closeModal()
        }
    }

    fun navigateToChainSwitchRedirect(chain: Modal.Model.Chain) {
        navController.navigateToChainSwitch(chain)
    }

    fun navigateToHelp() {
        navController.navigate(Route.WHAT_IS_WALLET.path)
    }
}