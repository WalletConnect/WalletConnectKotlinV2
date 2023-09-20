package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetIdentityUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.ui.model.UiState
import com.walletconnect.web3.modal.utils.getAddress
import com.walletconnect.web3.modal.utils.getChains
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
internal fun rememberAccountState(
    coroutineScope: CoroutineScope,
    navController: NavController
): AccountState = remember(coroutineScope, navController) {
    AccountState(coroutineScope, navController)
}

internal class AccountState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController
) {
    private val logger: Logger = wcKoinApp.koin.get()

    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()
    private val getIdentityUseCase: GetIdentityUseCase = wcKoinApp.koin.get()

    private val accountFlow = flow {
        val activeSession = getSessionTopicUseCase()?.let { Web3Modal.getActiveSessionByTopic(it) }
        if (activeSession != null) {
            val chains = activeSession.getChains()
            val selectedChain = chains.getSelectedChain(getSelectedChainUseCase())
            val address = activeSession.getAddress(selectedChain)
            val identity = getIdentityUseCase(address, selectedChain.id)
            accountData = AccountData(
                topic = activeSession.topic,
                address = address,
                balance = "",
                chains = chains,
                identity = identity
            )
            emit(UiState.Success(accountData))
        } else {
            emit(UiState.Error(Throwable("Active session not found")))
        }
    }.catch {
        logger.error(it)
        emit(UiState.Error(it))
    }
        .flowOn(Dispatchers.IO)

    lateinit var accountData: AccountData

    val accountState = accountFlow.stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

    val selectedChain = observeSelectedChainUseCase().map { savedChainId ->
        Web3Modal.chains.find { it.id == savedChainId } ?: Web3Modal.getSelectedChainOrFirst()
    }

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
    }
}