package com.walletconnect.web3.modal.ui.routes.account

import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.account.change_network.ChangeNetworkRoute
import com.walletconnect.web3.modal.utils.ScreenShotTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module

internal class ChangeNetworkRouteTest: ScreenShotTest("account/${Route.CHANGE_NETWORK.path}")  {

    private val viewModel: AccountViewModel = mockk()
    private val selectedChain: StateFlow<Modal.Model.Chain> = mockk()
    private val uiState: StateFlow<UiState<AccountData>> = mockk()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = mockk()

    @Before
    fun setup() {
        Web3Modal.chains = Web3ModalChainsPresets.ethChains.values.toList()
        every { viewModel.accountState } returns uiState
        every { uiState.value } returns UiState.Success(AccountData("0x2765d421FB91182490D602E671a", Web3ModalChainsPresets.ethChains.values.toList()))
        every { viewModel.selectedChain } returns selectedChain
        every { selectedChain.value } returns Web3ModalChainsPresets.ethChains["1"]!!
        every { getSelectedChainUseCase() } returns "1"
        every { viewModel.getSelectedChainOrFirst() } returns Web3ModalChainsPresets.ethChains.getOrElse("1") { throw IllegalStateException("Chain not found") }
        wcKoinApp.koin.loadModules(modules = listOf(module { single { getSelectedChainUseCase } }))

    }

    @Test
    fun `test ChangeNetworkRoute in LightMode`() = runRouteScreenShotTest(
        title = Route.CHANGE_NETWORK.title
    ) {
       ChangeNetworkRoute(accountViewModel = viewModel)
    }

    @Test
    fun `test ChangeNetworkRoute in DarkMode`() = runRouteScreenShotTest(
        title = Route.CHANGE_NETWORK.title,
        nightMode = NightMode.NIGHT
    ) {
        ChangeNetworkRoute(accountViewModel = viewModel)
    }

    @Test
    fun `test ChangeNetworkRoute in Landscape`() = runRouteScreenShotTest(
        title = Route.CHANGE_NETWORK.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        ChangeNetworkRoute(accountViewModel = viewModel)
    }
}