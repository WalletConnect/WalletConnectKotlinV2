@file:OptIn(ExperimentalCoroutinesApi::class)

package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.openWeb3Modal
import com.walletconnect.web3.modal.utils.getAddress
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

@Composable
fun rememberWeb3ButtonState(
    navController: NavController
): Web3ButtonState {
    return remember(navController) {
        Web3ButtonState(navController)
    }
}

class Web3ButtonState(
    private val navController: NavController
) {
    val isOpen
        get() = navController.currentBackStackEntryFlow.mapLatest { it.destination.route?.startsWith(Route.WEB3MODAL.path) ?: false }

    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()

    internal suspend fun getAccountButtonState(accountButtonType: AccountButtonType) = getActiveSession()?.let { activeSession ->
        val accounts = activeSession.namespaces.values.toList().flatMap { it.accounts }
        val chains = activeSession.namespaces.values.toList().flatMap { it.chains ?: listOf() }.map { Chain(it) }
        val selectedChain = chains.getSelectedChain(getSelectedChainUseCase())
        val address = accounts.getAddress(selectedChain)
        when(accountButtonType) {
            AccountButtonType.NORMAL -> AccountButtonState.Normal(address = address)
            AccountButtonType.MIXED -> AccountButtonState.Mixed(
                address = address,
                chainImageUrl = selectedChain.imageUrl,
                balance = ""
            )
        }

    }

    internal suspend fun getActiveSession() = withContext(Dispatchers.IO) {
        Web3Modal.getActiveSessionByTopic(getSessionTopicUseCase() ?: String.Empty)
    }

    internal fun getSelectedChain() = getSelectedChainUseCase()?.let { Chain(it) }

    internal fun openWeb3Modal() {
        navController.openWeb3Modal()
    }

}
