package com.walletconnect.web3.modal.ui.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.walletconnect.web3.modal.ui.components.internal.ErrorModalState
import com.walletconnect.web3.modal.ui.components.internal.LoadingModalState
import kotlinx.coroutines.flow.StateFlow

internal sealed class UiState<T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Loading<T>(val data: T? = null, val loadingState: LoadingState = LoadingState.REFRESH) : UiState<T>()
    data class Error<T>(val error: Throwable) : UiState<T>()
}

internal enum class LoadingState {
    REFRESH, APPEND
}

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
