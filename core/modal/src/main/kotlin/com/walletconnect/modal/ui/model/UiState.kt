package com.walletconnect.modal.ui.model

sealed class UiState<T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Loading<T>(val data: T? = null, val loadingState: LoadingState = LoadingState.REFRESH) : UiState<T>()
    data class Error<T>(val error: Throwable) : UiState<T>()
}

enum class LoadingState {
    REFRESH, APPEND
}
