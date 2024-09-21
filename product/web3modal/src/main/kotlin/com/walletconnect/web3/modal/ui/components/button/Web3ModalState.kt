package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pulse.domain.SendEventInterface
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.Session
import com.walletconnect.web3.modal.domain.usecase.GetEthBalanceUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionUseCase
import com.walletconnect.web3.modal.engine.Web3ModalEngine
import com.walletconnect.web3.modal.ui.components.ComponentDelegate
import com.walletconnect.web3.modal.ui.components.ComponentEvent
import com.walletconnect.web3.modal.ui.openWeb3Modal
import com.walletconnect.web3.modal.utils.getChainNetworkImageUrl
import com.walletconnect.web3.modal.utils.getChains
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Deprecated("com.walletconnect.web3.modal.ui.components.button.rememberWeb3ModalState has been deprecated. Please use com.reown.appkit.modal.ui.components.button.rememberAppKitState instead from - https://github.com/reown-com/reown-kotlin",
    ReplaceWith("rememberAppKitState(navController)")
)
@Composable
fun rememberWeb3ModalState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController
): Web3ModalState {
    return remember(navController) {
        Web3ModalState(coroutineScope, navController)
    }
}

@Deprecated("com.walletconnect.web3.modal.ui.components.button.Web3ModalState has been deprecated. Please use com.reown.appkit.modal.ui.components.button.AppKitState instead from - https://github.com/reown-com/reown-kotlin")
class Web3ModalState(
    coroutineScope: CoroutineScope,
    private val navController: NavController
) {
    private val logger: Logger = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()
    private val observeSessionTopicUseCase: ObserveSessionUseCase = wcKoinApp.koin.get()
    private val getSessionUseCase: GetSessionUseCase = wcKoinApp.koin.get()
    private val getEthBalanceUseCase: GetEthBalanceUseCase = wcKoinApp.koin.get()
    private val web3ModalEngine: Web3ModalEngine = wcKoinApp.koin.get()
    private val sendEventUseCase: SendEventInterface = wcKoinApp.koin.get()
    private val sessionTopicFlow = observeSessionTopicUseCase()

    val isOpen = ComponentDelegate.modalComponentEvent
        .map { event ->
            sendModalCloseOrOpenEvents(event)
            event.isOpen
        }
        .stateIn(coroutineScope, started = SharingStarted.Lazily, ComponentDelegate.isModalOpen)

    val isConnected = sessionTopicFlow
        .map { it != null && getSessionUseCase() != null }
        .map { Web3Modal.getAccount() != null }
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = false)

    internal val selectedChain = observeSelectedChainUseCase().map { savedChainId ->
        Web3Modal.chains.find { it.id == savedChainId }
    }

    internal val accountNormalButtonState = sessionTopicFlow.combine(selectedChain) { session, chain -> session to chain }
        .mapOrAccountState(AccountButtonType.NORMAL)
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = AccountButtonState.Loading)

    internal val accountMixedButtonState = sessionTopicFlow.combine(selectedChain) { session, chain -> session to chain }
        .mapOrAccountState(AccountButtonType.MIXED)
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = AccountButtonState.Loading)

    private fun Flow<Pair<Session?, Modal.Model.Chain?>>.mapOrAccountState(accountButtonType: AccountButtonType) =
        map { web3ModalEngine.getActiveSession()?.mapToAccountButtonState(accountButtonType) ?: AccountButtonState.Invalid }

    private fun sendModalCloseOrOpenEvents(event: ComponentEvent) {
        when {
            event.isOpen && isConnected.value -> sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.MODAL_OPEN, Properties(connected = true)))
            event.isOpen && !isConnected.value -> sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.MODAL_OPEN, Properties(connected = false)))
            !event.isOpen && isConnected.value -> sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.MODAL_CLOSE, Properties(connected = true)))
            !event.isOpen && !isConnected.value -> sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.MODAL_CLOSE, Properties(connected = false)))
        }
    }

    private suspend fun Session.mapToAccountButtonState(accountButtonType: AccountButtonType) = try {
        val chains = getChains()
        val selectedChain = chains.getSelectedChain(this.chain)
        val address = this.address
        when (accountButtonType) {
            AccountButtonType.NORMAL -> AccountButtonState.Normal(address = address)
            AccountButtonType.MIXED -> {
                val balance = getBalance(selectedChain, address)
                AccountButtonState.Mixed(
                    address = address,
                    chainImage = selectedChain.chainImage ?: getChainNetworkImageUrl(selectedChain.chainReference),
                    chainName = selectedChain.chainName,
                    balance = balance
                )
            }
        }
    } catch (e: Exception) {
        AccountButtonState.Invalid
    }

    private suspend fun getBalance(selectedChain: Modal.Model.Chain, address: String) =
        selectedChain.rpcUrl?.let { url -> getEthBalanceUseCase(selectedChain.token, url, address)?.valueWithSymbol }

    internal fun openWeb3Modal(shouldOpenChooseNetwork: Boolean = false, isActiveNetwork: Boolean = false) {
        if (shouldOpenChooseNetwork && isActiveNetwork) {
            sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CLICK_NETWORKS))
        }

        navController.openWeb3Modal(
            shouldOpenChooseNetwork = shouldOpenChooseNetwork,
            onError = { logger.error(it) }
        )
    }
}
