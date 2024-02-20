package com.walletconnect.web3.modal.ui.routes.account

import androidx.navigation.NavController
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Balance
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.account.account.AccountRoute
import com.walletconnect.web3.modal.utils.ScreenShotTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.koin.dsl.module

@Ignore
internal class AccountRouteTest: ScreenShotTest("account/${Route.ACCOUNT.path}") {

    private val navController: NavController = mockk()
    private val viewModel: AccountViewModel = mockk()
    private val uiState: StateFlow<UiState<AccountData>> = mockk()
    private val selectedChain: StateFlow<Modal.Model.Chain> = mockk()
    private val balance: StateFlow<Balance> = mockk()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = mockk()
    private val accountData = AccountData(
        address = "0x2765d421FB91182490D602E671a",
        chains = Web3ModalChainsPresets.ethChains.values.toList()
    )

    @Before
    fun setup() {
        Web3Modal.chains = Web3ModalChainsPresets.ethChains.values.toList()
        every { viewModel.accountState } returns uiState
        every { viewModel.selectedChain } returns selectedChain
        every { viewModel.balanceState } returns balance
        every { selectedChain.value } returns Web3ModalChainsPresets.ethChains["1"]!!
        every { balance.value } returns Balance(Web3ModalChainsPresets.ethToken, "0x0")
        every { getSelectedChainUseCase() } returns "1"

        val modules = listOf(
            module { single { getSelectedChainUseCase } }
        )
        wcKoinApp.koin.loadModules(modules = modules)

    }

    @Test
    fun `test AccountRoute with Loading UiState in LightMode`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title
    ) {
        every { uiState.value } returns UiState.Loading()
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Loading UiState in DarkMode`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title,
        nightMode = NightMode.NIGHT
    ) {
        every { uiState.value } returns UiState.Loading()
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Loading UiState in Landscape`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        every { uiState.value } returns UiState.Loading()
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Error UiState in LightMode`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title
    ) {
        every { uiState.value } returns UiState.Error(Throwable("Something goes wrong"))
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Error UiState in DarkMode`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title,
        nightMode = NightMode.NIGHT
    ) {
        every { uiState.value } returns UiState.Error(Throwable("Something goes wrong"))
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Error UiState in Landscape`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        every { uiState.value } returns UiState.Error(Throwable("Something goes wrong"))
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Success UiState in LightMode`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title
    ) {
        every { uiState.value } returns UiState.Success(accountData)
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Success UiState in DarkMode`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title,
        nightMode = NightMode.NIGHT
    ) {
        every { uiState.value } returns UiState.Success(accountData)
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }

    @Test
    fun `test AccountRoute with Success UiState in Landscape`() = runRouteScreenShotTest(
        title = Route.ACCOUNT.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        every { uiState.value } returns UiState.Success(accountData)
        AccountRoute(navController = navController, accountViewModel = viewModel)
    }
}
