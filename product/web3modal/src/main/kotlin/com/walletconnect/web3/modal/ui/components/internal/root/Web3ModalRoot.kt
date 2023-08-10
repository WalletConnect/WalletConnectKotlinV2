package com.walletconnect.web3.modal.ui.components.internal.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.components.internal.commons.BackArrowIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.QuestionMarkIcon
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
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
    val title by rootState.title.collectAsState()

    Column(verticalArrangement = Arrangement.Bottom) {
        ProvideWeb3ModalThemeComposition {
            Web3ModalRoot(rootState, title, closeModal, content)
        }
    }
}

@Composable
private fun Web3ModalRoot(
    rootState: Web3ModalRootState,
    title: String?,
    closeModal: () -> Unit,
    content: @Composable () -> Unit
) {
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

@Composable
private fun TopBarStartIcon(
    rootState: Web3ModalRootState
) {
    if (rootState.canPopUp) {
        BackArrowIcon(onClick = rootState::popUp)
    } else {
        QuestionMarkIcon(onClick = rootState::navigateToHelp)
    }
}

@Composable
@UiModePreview
private fun PreviewWeb3ModalRoot() {
    val content: @Composable () -> Unit = { Box(modifier = Modifier.size(200.dp)) }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val rootState = rememberWeb3ModalRootState(coroutineScope = scope, navController = navController)

    MultipleComponentsPreview(
        { Web3ModalRoot(rootState, null, {}, { content() }) },
        { Web3ModalRoot(rootState, "Top Bar Title", {}, { content() }) }
    )
}

