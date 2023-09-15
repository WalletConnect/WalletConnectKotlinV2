package com.walletconnect.web3.modal.ui.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.walletconnect.web3.modal.ui.components.internal.ErrorModalState
import com.walletconnect.web3.modal.ui.components.internal.LoadingModalState
import kotlinx.coroutines.flow.Flow

internal sealed class UiState<T> {
    data class Success<T>(val data: T): UiState<T>()
    data class Loading<T>(val data: T? = null): UiState<T>()
    data class Error<T>(val error: Throwable): UiState<T>()
}

@Composable
internal fun<T> Flow<UiState<T>>.collectUiState(data: T? = null) = collectAsState(initial = UiState.Loading(data))

@Composable
internal fun<T> UiStateBuilder(
    uiStateFlow: Flow<UiState<T>>,
    onError: @Composable (Throwable) -> Unit = { ErrorModalState {} },
    onLoading: @Composable (T?) -> Unit = { LoadingModalState() },
    onSuccess: @Composable (T) -> Unit
) {
    val uiState by uiStateFlow.collectUiState()
    AnimatedContent(
        targetState = uiState,
        label = "UiStateBuilder $uiState"
    ) { state ->
        when(state) {
            is UiState.Error -> onError(state.error)
            is UiState.Loading -> onLoading(state.data)
            is UiState.Success -> onSuccess(state.data)
        }
    }
}
