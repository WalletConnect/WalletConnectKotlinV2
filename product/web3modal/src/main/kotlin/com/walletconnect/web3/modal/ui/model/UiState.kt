package com.walletconnect.web3.modal.ui.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.web3.modal.ui.components.internal.ErrorModalState
import com.walletconnect.web3.modal.ui.components.internal.LoadingModalState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun <T> UiStateBuilder(
    uiStateFlow: StateFlow<UiState<T>>,
    onError: @Composable (Throwable) -> Unit = { ErrorModalState {} },
    onLoading: @Composable (T?) -> Unit = { LoadingModalState() },
    onSuccess: @Composable (T) -> Unit
) {
    val uiState by uiStateFlow.collectAsState()
    AnimatedContent(
        targetState = uiState,
        label = "UiStateBuilder $uiState",
        transitionSpec = { fadeIn() + slideInHorizontally { it / 2 } togetherWith fadeOut() }
    ) { state ->
        when (state) {
            is UiState.Error -> onError(state.error)
            is UiState.Loading -> onLoading(state.data)
            is UiState.Success -> onSuccess(state.data)
        }
    }
}
