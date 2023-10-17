package com.walletconnect.web3.modal.ui.components.internal.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@Composable
internal fun rememberSnackBarState(coroutineScope: CoroutineScope) = remember { SnackBarState(coroutineScope) }

@Stable
internal class SnackBarState(
    private val scope: CoroutineScope
) {

    private val mutex = Mutex()

    internal var currentSnackBarState by mutableStateOf<SnackBarEvent?>(value = null)
        private set

    fun showSuccessSnack(message: String, duration: SnackbarDuration = SnackbarDuration.SHORT) {
        scope.launch {
            show(SnackBarEventType.SUCCESS, message, duration = duration)
        }
    }

    fun showErrorSnack(message: String, duration: SnackbarDuration = SnackbarDuration.SHORT) {
        scope.launch {
            show(SnackBarEventType.ERROR, message, duration = duration)
        }
    }

    fun showInfoSnack(message: String, duration: SnackbarDuration = SnackbarDuration.SHORT) {
        scope.launch {
            show(SnackBarEventType.INFO, message, duration = duration)
        }
    }

    suspend fun show(
        type: SnackBarEventType,
        message: String,
        duration: SnackbarDuration
    ): SnackBarResultState = mutex.withLock {
        try {
            return suspendCancellableCoroutine { continuation ->
                currentSnackBarState = SnackBarEventImpl(type, message, duration, continuation)
            }
        } finally {
            currentSnackBarState = null
        }
    }

    @Stable
    class SnackBarEventImpl(
        override val type: SnackBarEventType,
        override val message: String,
        override val duration: SnackbarDuration,
        private val continuation: CancellableContinuation<SnackBarResultState>
    ): SnackBarEvent {
        override fun dismiss() {
            if (continuation.isActive) continuation.resume(SnackBarResultState.Dismissed)
        }
    }


}