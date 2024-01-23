package com.walletconnect.web3.modal.ui.routes.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.usecase.GetEthBalanceUseCase
import com.walletconnect.web3.modal.domain.usecase.GetIdentityUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionTopicUseCase
import com.walletconnect.web3.modal.ui.model.UiState
import com.walletconnect.web3.modal.ui.navigation.Navigator
import com.walletconnect.web3.modal.ui.navigation.NavigatorImpl
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.account.toChainSwitchPath
import com.walletconnect.web3.modal.utils.EthUtils
import com.walletconnect.web3.modal.utils.createAddEthChainParams
import com.walletconnect.web3.modal.utils.createSwitchChainParams
import com.walletconnect.web3.modal.utils.getAddress
import com.walletconnect.web3.modal.utils.getChains
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AccountViewModel: ViewModel(), Navigator by NavigatorImpl() {
    private val logger: Logger = wcKoinApp.koin.get()

    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val saveSessionTopicUseCase: SaveSessionTopicUseCase = wcKoinApp.koin.get()
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
            showError(it.localizedMessage)
            logger.error(it)
            emit(UiState.Error(it))
        }

    lateinit var accountData: AccountData

    val accountState = accountDataFlow.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

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
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)

    fun disconnect() {
        closeModal()
        Web3Modal.disconnect(
            onSuccess = {},
            onError = {
                showError(it.throwable.localizedMessage)
                logger.error(it.throwable)
            }
        )
    }


    fun changeActiveChain(chain: Modal.Model.Chain) = viewModelScope.launch {
        if (accountData.chains.contains(chain)) {
            saveChainSelectionUseCase(chain.id)
            popBackStack()
        } else {
            navigateTo(chain.toChainSwitchPath())
        }
    }

    suspend fun updatedSessionAfterChainSwitch(
        chain: Modal.Model.Chain,
        updatedSession: Modal.Model.UpdatedSession
    ) {
        if (updatedSession.getChains().contains(chain)) {
            saveChainSelectionUseCase(chain.id)
            saveSessionTopicUseCase(updatedSession.topic)
            popBackStack(path = Route.CHANGE_NETWORK.path, inclusive = true)
        }
    }

    suspend fun switchChain(
        to: Modal.Model.Chain,
        openConnectedWallet: (String) -> Unit
    ) {
        val onError: (String?) -> Unit = { showError(it ?: "Something went wrong") }
        val isChainApproved = accountData.chains.contains(to)
        if (!isChainApproved && to.optionalMethods.contains(EthUtils.walletAddEthChain)) {
            addEthChain(to, openConnectedWallet, onError)
        } else {
            switchEthChain(to, openConnectedWallet, onError)
        }
    }

    private suspend fun switchEthChain(
        to: Modal.Model.Chain,
        openConnectedWallet: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        Web3Modal.request(
            Modal.Params.Request(
                method = EthUtils.walletSwitchEthChain,
                params = createSwitchChainParams(to)
            )
        )
            .onSuccess { openConnectedWallet(prepareRedirectUri()) }
            .onFailure { onError(it.message) }
    }

    private suspend fun addEthChain(
        to: Modal.Model.Chain,
        onSuccess: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        Web3Modal.request(
            Modal.Params.Request(
                method = EthUtils.walletAddEthChain,
                params = createAddEthChainParams(to)
            )
        )
            .onSuccess { onSuccess(prepareRedirectUri()) }
            .onFailure { onError(it.message) }
    }

    private suspend fun prepareRedirectUri(): String {
        var redirectUrl = ""
        val session = activeSessionFlow.first()
        session?.let {
            redirectUrl = "${it.redirect}wc?sessionTopic=${it.topic}"
        }
        return redirectUrl
    }

    fun navigateToHelp() {
        navigateTo(Route.WHAT_IS_WALLET.path)
    }
}
