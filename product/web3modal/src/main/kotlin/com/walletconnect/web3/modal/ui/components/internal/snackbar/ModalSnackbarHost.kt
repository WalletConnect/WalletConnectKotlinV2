package com.walletconnect.web3.modal.ui.components.internal.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun ModalSnackBarHost(
    state: SnackBarState,
    content: @Composable () -> Unit
) {
    val currentSnackBarData = state.currentSnackBarState
    LaunchedEffect(currentSnackBarData) {
        if (currentSnackBarData != null) {
            delay(currentSnackBarData.duration.value)
            currentSnackBarData.dismiss()
        }
    }
    val isVisibleSnackBar = currentSnackBarData != null

    CompositionLocalProvider(
        LocalSnackBarHandler provides state
    ) {
        Box {
            content()
            AnimatedVisibility(
                visible = isVisibleSnackBar,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .align(Alignment.TopCenter),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ModalSnackBar(currentSnackBarData)
            }
        }
    }
}

internal val LocalSnackBarHandler = staticCompositionLocalOf<SnackBarState>() {
    error("CompositionLocal SnackBarComponent not present")
}
