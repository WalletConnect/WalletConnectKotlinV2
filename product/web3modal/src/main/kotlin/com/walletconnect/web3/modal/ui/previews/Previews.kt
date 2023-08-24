package com.walletconnect.web3.modal.ui.previews

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.root.rememberWeb3ModalRootState
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun Web3ModalPreview(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    ProvideWeb3ModalThemeComposition {
        val rootState = rememberWeb3ModalRootState(coroutineScope = rememberCoroutineScope(), navController = rememberNavController())
        Web3ModalRoot(rootState = rootState, closeModal = {}, title = title) {
            content()
        }
    }
}

@Composable
internal fun ComponentPreview(
    content: @Composable () -> Unit
) {
    ProvideWeb3ModalThemeComposition {
        Column(modifier = Modifier.background(Web3ModalTheme.colors.background.color100)) {
            content()
        }
    }
}

@Composable
internal fun MultipleComponentsPreview(
    vararg content: @Composable () -> Unit
) {
    ProvideWeb3ModalThemeComposition {
        Column {
            content.forEach {
                VerticalSpacer(height = 5.dp)
                Box(modifier = Modifier.background(Web3ModalTheme.colors.background.color100)) { it() }
                VerticalSpacer(height = 5.dp)
            }
        }
    }
}

@LightTheme
@DarkTheme
internal annotation class UiModePreview

@Preview(uiMode = UI_MODE_NIGHT_NO)
internal annotation class LightTheme

@Preview(uiMode = UI_MODE_NIGHT_YES)
internal annotation class DarkTheme
