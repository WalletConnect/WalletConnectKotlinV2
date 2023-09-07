package com.walletconnect.web3.modal.ui.components.internal.root

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.rememberTestNavController
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class Web3ModalRootStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var state: Web3ModalRootState

    @Test
    fun web3modalRootState_currentDestination() = runTest {
        var currentDestinationPath: String? = null

        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = remember(navController) {
                Web3ModalRootState(
                    coroutineScope = backgroundScope,
                    navController = navController
                )
            }

            currentDestinationPath = state.currentDestinationFlow.collectAsState(null).value?.destination?.route

            LaunchedEffect(Unit) {
                navController.setCurrentDestination(Route.QR_CODE.path)
            }
        }

        assertEquals(Route.QR_CODE.path, currentDestinationPath)
    }

    @Test
    fun web3ModalRootState_canPopup_fromInitDestination() = runTest {
        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = rememberWeb3ModalRootState(coroutineScope = backgroundScope, navController = navController)
        }

        assertEquals(false, state.canPopUp)
    }

    @Test
    fun web3ModalRootState_canPopup_fromQRCode() = runTest {
        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = rememberWeb3ModalRootState(coroutineScope = backgroundScope, navController = navController)

            LaunchedEffect(Unit) {
                navController.setCurrentDestination(Route.QR_CODE.path)
            }
        }

        assertEquals(true, state.canPopUp)
    }

    @Test
    fun web3ModalRouteState_popUp_shouldNavigateBack() = runTest {
        var currentDestinationPath: String? = null

        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = rememberWeb3ModalRootState(coroutineScope = backgroundScope, navController = navController)

            currentDestinationPath = state.currentDestinationFlow.collectAsState(null).value?.destination?.route

            LaunchedEffect(Unit) {
                navController.navigate(Route.QR_CODE.path)
                state.popUp()
            }
        }

        assertEquals(Route.CONNECT_YOUR_WALLET.path, currentDestinationPath)
    }

    @Test
    fun web3ModalRouteState_title_shouldBeTakenFromEnum() = runTest {
        var title: String? = null

        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = rememberWeb3ModalRootState(coroutineScope = backgroundScope, navController = navController)

            title = state.title.collectAsState(initial = null).value
        }

        assertEquals(Route.CONNECT_YOUR_WALLET.title, title)
    }

    @Test
    fun web3ModalRouteState_title_shouldBeTakenFromArg() = runTest {
        var title: String? = null

        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = rememberWeb3ModalRootState(coroutineScope = backgroundScope, navController = navController)

            title = state.title.collectAsState(initial = null).value

            LaunchedEffect(Unit) {
                navController.navigate(Route.REDIRECT.path + "&" + "title")
            }
        }

        assertEquals("title", title)
    }

    @Test
    fun web3ModalRouteState_navigateToHelp() = runTest {
        var currentDestinationPath: String? = null

        composeTestRule.setContent {
            val navController = rememberTestNavController()
            state = rememberWeb3ModalRootState(coroutineScope = backgroundScope, navController = navController)

            currentDestinationPath = state.currentDestinationFlow.collectAsState(null).value?.destination?.route

            LaunchedEffect(Unit) {
                state.navigateToHelp()
            }
        }

        assertEquals(Route.HELP.path, currentDestinationPath)
    }
}
