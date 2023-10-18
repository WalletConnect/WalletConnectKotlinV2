package com.walletconnect.web3.modal.ui.components.internal.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.components.internal.commons.BackArrowIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.QuestionMarkIcon
import com.walletconnect.web3.modal.ui.components.internal.snackbar.ModalSnackBarHost
import com.walletconnect.web3.modal.ui.components.internal.snackbar.SnackBarEventType
import com.walletconnect.web3.modal.ui.components.internal.snackbar.SnackBarState
import com.walletconnect.web3.modal.ui.components.internal.snackbar.rememberSnackBarState
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun Web3ModalRoot(
    navController: NavHostController,
    closeModal: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val rootState = rememberWeb3ModalRootState(coroutineScope = scope, navController = navController)
    val snackBarState = rememberSnackBarState(coroutineScope = scope)
    val title by rootState.title.collectAsState(null)

    Column(verticalArrangement = Arrangement.Bottom) {
        ProvideWeb3ModalThemeComposition {
            Web3ModalRoot(rootState, snackBarState, title, closeModal, content)
        }
    }
}

@Composable
internal fun Web3ModalRoot(
    rootState: Web3ModalRootState,
    snackBarState: SnackBarState,
    title: String?,
    closeModal: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalSnackBarHost(snackBarState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Web3ModalTheme.colors.background.color100)
        ) {
            title?.let { title ->
                Web3ModalTopBar(
                    title = title,
                    startIcon = { TopBarStartIcon(rootState) },
                    onCloseIconClick = closeModal
                )
                FullWidthDivider()
            }
            content()
        }
    }
}

@Composable
private fun TopBarStartIcon(
    rootState: Web3ModalRootState
) {
    if (rootState.canPopUp) {
        BackArrowIcon(onClick = rootState::popUp)
    } else {
        when (rootState.currentDestinationRoute) {
            Route.CONNECT_YOUR_WALLET.path -> QuestionMarkIcon(onClick = rootState::navigateToHelp)
        }
    }
}

@Composable
@UiModePreview
private fun PreviewWeb3ModalRoot() {
    val content: @Composable () -> Unit = { Box(modifier = Modifier.size(200.dp)) }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val rootState = rememberWeb3ModalRootState(coroutineScope = scope, navController = navController)
    val snackBarState = rememberSnackBarState(coroutineScope = scope)

    MultipleComponentsPreview(
        { Web3ModalRoot(rootState, snackBarState, null, {}, { content() }) },
        { Web3ModalRoot(rootState, snackBarState, "Top Bar Title", {}, { content() }) }
    )
}

