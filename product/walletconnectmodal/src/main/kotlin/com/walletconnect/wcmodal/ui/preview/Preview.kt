package com.walletconnect.wcmodal.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.walletconnect.wcmodal.ui.components.ModalRoot
import com.walletconnect.wcmodal.ui.theme.ModalTheme
import com.walletconnect.wcmodal.ui.theme.ProvideModalThemeComposition

@Composable
internal fun ModalPreview(
    content: @Composable () -> Unit,
) {
    ModalRoot(
        navController = rememberNavController(),
        closeModal = {}
    ) {
        content()
    }
}

@Composable
internal fun ComponentPreview(
    content: @Composable ColumnScope.() -> Unit
) {
    ProvideModalThemeComposition {
        Column(modifier = Modifier.background(ModalTheme.colors.background)) {
            content()
        }
    }
}
